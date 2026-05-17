package com.example.zero.healthcare.journal.service;

import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import com.example.zero.healthcare.service.JournalService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JournalRepository journalRepository;
    @InjectMocks private JournalService journalService;

    private User stubUser(Long id) {
        User user = new User("test@example.com", "token", "tester");
        // reflection으로 id 세팅 대신 save 반환값 stubbing으로 처리
        return user;
    }

    private PreConditionDto validPreCondition() {
        return new PreConditionDto(5, 7, 6, 4, 8);
    }

    @Test
    @DisplayName("유효한 요청으로 일지를 생성하면 journalId와 createdAt을 반환한다")
    void createJournal_withValidRequest_returnsJournalIdAndPersists() {
        User user = new User("test@example.com", "token", "tester");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        WorkoutJournal savedJournal = WorkoutJournal.builder()
                .authorId(1L).preJointMusclePain(5).preSleepHours(7)
                .preSleepQuality(6).prePreviousFatigue(4).preOverallCondition(8).build();
        given(journalRepository.save(any())).willReturn(savedJournal);

        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), List.of());
        CreateJournalResponse response = journalService.createJournal(req);

        assertThat(response).isNotNull();
        verify(journalRepository).save(any(WorkoutJournal.class));
    }

    @Test
    @DisplayName("painRecords가 빈 배열이면 pain record 없이 일지만 저장된다")
    void createJournal_withEmptyPainRecords_persistsJournalOnly() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        WorkoutJournal saved = WorkoutJournal.builder()
                .authorId(1L).preJointMusclePain(5).preSleepHours(7)
                .preSleepQuality(6).prePreviousFatigue(4).preOverallCondition(8).build();
        given(journalRepository.save(any())).willReturn(saved);

        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), List.of());
        journalService.createJournal(req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getPainRecords()).isEmpty();
    }

    @Test
    @DisplayName("painRecords가 null이어도 일지는 정상 저장되고 pain record는 0건이다")
    void createJournal_withNullPainRecords_persistsJournalOnly() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        WorkoutJournal saved = WorkoutJournal.builder()
                .authorId(1L).preJointMusclePain(5).preSleepHours(7)
                .preSleepQuality(6).prePreviousFatigue(4).preOverallCondition(8).build();
        given(journalRepository.save(any())).willReturn(saved);

        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), null);
        journalService.createJournal(req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getPainRecords()).isEmpty();
    }

    @Test
    @DisplayName("저장된 모든 JournalPainRecord의 timing은 PRE이다")
    void createJournal_persistedPainRecord_hasTimingPre() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        WorkoutJournal saved = WorkoutJournal.builder()
                .authorId(1L).preJointMusclePain(5).preSleepHours(7)
                .preSleepQuality(6).prePreviousFatigue(4).preOverallCondition(8).build();
        given(journalRepository.save(any())).willReturn(saved);

        List<PainRecordDto> records = List.of(
                new PainRecordDto("SHOULDER", "LEFT", 7),
                new PainRecordDto("KNEE", "RIGHT", 4)
        );
        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), records);
        journalService.createJournal(req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getPainRecords())
                .hasSize(2)
                .allMatch(r -> r.getTiming() == PainTiming.PRE);
    }

    @Test
    @DisplayName("존재하지 않는 userId이면 EntityNotFoundException을 던진다")
    void createJournal_unknownUser_throwsEntityNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        CreateJournalRequest req = new CreateJournalRequest(999L, null, validPreCondition(), null);

        assertThatThrownBy(() -> journalService.createJournal(req))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
