package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class JournalDetailDto {

    private final Long journalId;
    private final Long folderId;
    private final LocalDateTime createdAt;
    private final LocalDateTime postRecordedAt;

    private final Integer preJointMusclePain;
    private final Integer preSleepHours;
    private final Integer preSleepQuality;
    private final Integer prePreviousFatigue;
    private final Integer preOverallCondition;

    private final Integer postJointMusclePain;
    private final Integer postIntensityFit;
    private final Integer postGoalAchieved;
    private final Integer postDizziness;
    private final Integer postMood;

    private final String content;
    private final List<PainRecordResponseDto> painRecords;

    public JournalDetailDto(WorkoutJournal journal) {
        this.journalId = journal.getId();
        this.folderId = journal.getFolderId();
        this.createdAt = journal.getCreatedAt();
        this.postRecordedAt = journal.getPostRecordedAt();

        this.preJointMusclePain = journal.getPreJointMusclePain();
        this.preSleepHours = journal.getPreSleepHours();
        this.preSleepQuality = journal.getPreSleepQuality();
        this.prePreviousFatigue = journal.getPrePreviousFatigue();
        this.preOverallCondition = journal.getPreOverallCondition();

        this.postJointMusclePain = journal.getPostJointMusclePain();
        this.postIntensityFit = journal.getPostIntensityFit();
        this.postGoalAchieved = journal.getPostGoalAchieved();
        this.postDizziness = journal.getPostDizziness();
        this.postMood = journal.getPostMood();

        this.content = journal.getContent();
        this.painRecords = journal.getPainRecords().stream()
                .map(PainRecordResponseDto::new)
                .collect(Collectors.toList());
    }
}
