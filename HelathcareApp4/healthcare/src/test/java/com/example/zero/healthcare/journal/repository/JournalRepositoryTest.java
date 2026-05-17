package com.example.zero.healthcare.journal.repository;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalRepositoryTest {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManager entityManager;

    private User saveUser() {
        User user = new User("test-" + UUID.randomUUID() + "@example.com", "token", "tester");
        return userRepository.save(user);
    }

    private WorkoutJournal buildJournal(Long authorId) {
        return WorkoutJournal.builder()
                .authorId(authorId)
                .preJointMusclePain(5)
                .preSleepHours(7)
                .preSleepQuality(6)
                .prePreviousFatigue(4)
                .preOverallCondition(8)
                .build();
    }

    @Test
    @DisplayName("일지 저장 시 painRecords가 cascade PERSIST로 함께 저장된다")
    void save_withPainRecords_persistsCascade() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());

        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.SHOULDER).side(BodySide.LEFT).painLevel(7).build());
        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.KNEE).side(BodySide.RIGHT).painLevel(4).build());

        WorkoutJournal saved = journalRepository.save(journal);
        em.flush();
        em.clear();

        WorkoutJournal found = journalRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPainRecords()).hasSize(2);
    }

    @Test
    @DisplayName("findByAuthorId로 해당 유저의 일지를 조회한다")
    void findByAuthorId_returnsJournalsOfAuthor() {
        User user = saveUser();
        journalRepository.save(buildJournal(user.getId()));
        journalRepository.save(buildJournal(user.getId()));
        em.flush();
        em.clear();

        List<WorkoutJournal> journals = journalRepository.findByAuthorId(user.getId());

        assertThat(journals).hasSize(2);
    }

    @Test
    @DisplayName("delete() 호출 시 workout_journal은 물리 삭제되지 않고 deleted_at이 세팅된다")
    void delete_softDeletesJournal() {
        User user = saveUser();
        WorkoutJournal saved = journalRepository.save(buildJournal(user.getId()));
        em.flush();

        journalRepository.delete(saved);
        em.flush();
        em.clear();

        // @SQLRestriction 우회: 네이티브 쿼리로 직접 조회
        Long count = (Long) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM workout_journal WHERE id = :id AND deleted_at IS NOT NULL")
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("delete() 이후 journal_pain_record 행은 물리 삭제되지 않고 남는다")
    void delete_painRecordsNotPhysicallyDeleted() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());
        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.KNEE).side(BodySide.LEFT).painLevel(5).build());

        WorkoutJournal saved = journalRepository.save(journal);
        em.flush();
        em.clear(); // session 초기화 — pain records가 session에 없는 상태에서 delete

        // LAZY 로딩: findById는 journal만 session에 로드(pain records는 비로드)
        WorkoutJournal toDelete = journalRepository.findById(saved.getId()).orElseThrow();
        journalRepository.delete(toDelete);
        em.flush(); // journal만 session에 있으므로 TransientPropertyValueException 없음
        em.clear();

        Long count = (Long) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM journal_pain_record WHERE journal_id = :jid")
                .setParameter("jid", saved.getId())
                .getSingleResult();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("soft delete된 일지는 findByAuthorId 결과에서 자동 제외된다")
    void findByAuthorId_excludesSoftDeletedJournals() {
        User user = saveUser();
        WorkoutJournal active = journalRepository.save(buildJournal(user.getId()));
        WorkoutJournal toDelete = journalRepository.save(buildJournal(user.getId()));
        em.flush();

        journalRepository.delete(toDelete);
        em.flush();
        em.clear();

        List<WorkoutJournal> journals = journalRepository.findByAuthorId(user.getId());

        assertThat(journals).hasSize(1);
        assertThat(journals.get(0).getId()).isEqualTo(active.getId());
    }
}
