package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import lombok.Getter;

@Getter
public class PainRecordResponseDto {

    private final String timing;
    private final String bodyPart;
    private final String side;
    private final Integer painLevel;

    public PainRecordResponseDto(JournalPainRecord record) {
        this.timing = record.getTiming().name();
        this.bodyPart = record.getBodyPart().name();
        this.side = record.getSide().name();
        this.painLevel = record.getPainLevel();
    }
}
