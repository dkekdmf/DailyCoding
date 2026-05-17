package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalPostRequest;
import com.example.zero.healthcare.exception.journal.JournalNotFoundException;
import com.example.zero.healthcare.exception.journal.PostAlreadyRecordedException;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final UserRepository userRepository;
    private final JournalRepository journalRepository;

    @Transactional
    public CreateJournalResponse createJournal(CreateJournalRequest request) {
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.getUserId()));

        WorkoutJournal journal = WorkoutJournal.builder()
                .folderId(request.getFolderId())
                .authorId(request.getUserId())
                .preJointMusclePain(request.getPreCondition().getJointMusclePain())
                .preSleepHours(request.getPreCondition().getSleepHours())
                .preSleepQuality(request.getPreCondition().getSleepQuality())
                .prePreviousFatigue(request.getPreCondition().getPreviousFatigue())
                .preOverallCondition(request.getPreCondition().getOverallCondition())
                .build();

        List<PainRecordDto> painRecords = request.getPainRecords();
        if (painRecords != null) {
            for (PainRecordDto dto : painRecords) {
                journal.addPainRecord(JournalPainRecord.builder()
                        .timing(PainTiming.PRE)
                        .bodyPart(BodyPart.valueOf(dto.getBodyPart()))
                        .side(BodySide.valueOf(dto.getSide()))
                        .painLevel(dto.getPainLevel())
                        .build());
            }
        }

        WorkoutJournal saved = journalRepository.save(journal);
        return new CreateJournalResponse(saved.getId(), saved.getCreatedAt());
    }

    @Transactional
    public CreateJournalResponse updatePostCondition(Long journalId, UpdateJournalPostRequest request) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new JournalNotFoundException());

        if (journal.getPostRecordedAt() != null) {
            throw new PostAlreadyRecordedException();
        }

        journal.applyPostCondition(
                request.getPostCondition().getJointMusclePain(),
                request.getPostCondition().getIntensityFit(),
                request.getPostCondition().getGoalAchieved(),
                request.getPostCondition().getDizziness(),
                request.getPostCondition().getMood(),
                request.getContent()
        );

        List<PainRecordDto> painRecords = request.getPainRecords();
        if (painRecords != null) {
            for (PainRecordDto dto : painRecords) {
                journal.addPainRecord(JournalPainRecord.builder()
                        .timing(PainTiming.POST)
                        .bodyPart(BodyPart.valueOf(dto.getBodyPart()))
                        .side(BodySide.valueOf(dto.getSide()))
                        .painLevel(dto.getPainLevel())
                        .build());
            }
        }

        return new CreateJournalResponse(journal.getId(), journal.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<JournalSummaryDto> getMyJournals(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return journalRepository.findByAuthorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(JournalSummaryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JournalDetailDto getJournalDetail(Long journalId) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(JournalNotFoundException::new);
        return new JournalDetailDto(journal);
    }
}
