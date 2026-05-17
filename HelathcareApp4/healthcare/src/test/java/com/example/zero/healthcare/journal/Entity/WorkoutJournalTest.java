package com.example.zero.healthcare.journal.Entity;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutJournalTest {

    private WorkoutJournal buildJournal() {
        return WorkoutJournal.builder()
                .authorId(1L)
                .preJointMusclePain(5)
                .preSleepHours(7)
                .preSleepQuality(6)
                .prePreviousFatigue(4)
                .preOverallCondition(8)
                .build();
    }

    private JournalPainRecord buildRecord(BodyPart part, BodySide side) {
        return JournalPainRecord.builder()
                .timing(PainTiming.PRE)
                .bodyPart(part)
                .side(side)
                .painLevel(5)
                .build();
    }

    @Test
    @DisplayName("addPainRecord는 painRecords 리스트에 추가하고 양방향 연관을 설정한다")
    void addPainRecord_addsToListAndSetsJournal() {
        WorkoutJournal journal = buildJournal();
        JournalPainRecord record = buildRecord(BodyPart.SHOULDER, BodySide.LEFT);

        journal.addPainRecord(record);

        assertThat(journal.getPainRecords()).hasSize(1);
        assertThat(journal.getPainRecords().get(0)).isSameAs(record);
        assertThat(record.getJournal()).isSameAs(journal);
    }

    @Test
    @DisplayName("addPainRecord를 여러 번 호출하면 모두 리스트에 추가된다")
    void addPainRecord_multiple_addsAll() {
        WorkoutJournal journal = buildJournal();

        journal.addPainRecord(buildRecord(BodyPart.SHOULDER, BodySide.LEFT));
        journal.addPainRecord(buildRecord(BodyPart.KNEE, BodySide.RIGHT));

        assertThat(journal.getPainRecords()).hasSize(2);
        assertThat(journal.getPainRecords()).allMatch(r -> r.getJournal() == journal);
    }

    @Test
    @DisplayName("신규 일지는 post 필드가 모두 null이고 postRecordedAt도 null이다")
    void newJournal_postFieldsAreNull() {
        WorkoutJournal journal = buildJournal();

        assertThat(journal.getPostJointMusclePain()).isNull();
        assertThat(journal.getPostIntensityFit()).isNull();
        assertThat(journal.getPostGoalAchieved()).isNull();
        assertThat(journal.getPostDizziness()).isNull();
        assertThat(journal.getPostMood()).isNull();
        assertThat(journal.getPostRecordedAt()).isNull();
        assertThat(journal.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("applyPostCondition은 post 필드를 세팅하고 postRecordedAt을 기록한다")
    void applyPostCondition_setsPostFieldsAndRecordedAt() {
        WorkoutJournal journal = buildJournal();

        journal.applyPostCondition(6, 7, 8, 2, 9, "오늘 컨디션 좋았다");

        assertThat(journal.getPostJointMusclePain()).isEqualTo(6);
        assertThat(journal.getPostIntensityFit()).isEqualTo(7);
        assertThat(journal.getPostGoalAchieved()).isEqualTo(8);
        assertThat(journal.getPostDizziness()).isEqualTo(2);
        assertThat(journal.getPostMood()).isEqualTo(9);
        assertThat(journal.getContent()).isEqualTo("오늘 컨디션 좋았다");
        assertThat(journal.getPostRecordedAt()).isNotNull();
    }
}
