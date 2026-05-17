package com.example.zero.healthcare.dto.journal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateJournalPostRequest {

    @NotNull
    private Long userId;

    @NotNull @Valid
    private PostConditionDto postCondition;

    @Valid
    private List<PainRecordDto> painRecords;

    @Size(max = 5000)
    private String content;
}
