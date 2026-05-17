package com.example.zero.healthcare.dto.journal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateJournalResponse {
    private Long journalId;
    private LocalDateTime createdAt;
}
