package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JournalSummaryDto {

    private final Long journalId;
    private final LocalDateTime createdAt;
    private final boolean postRecorded;
    private final Integer preOverallCondition;
    private final String contentPreview;

    public JournalSummaryDto(WorkoutJournal journal) {
        this.journalId = journal.getId();
        this.createdAt = journal.getCreatedAt();
        this.postRecorded = journal.getPostRecordedAt() != null;
        this.preOverallCondition = journal.getPreOverallCondition();
        String c = journal.getContent();
        this.contentPreview = (c != null && c.length() > 100) ? c.substring(0, 100) : c;
    }
}
