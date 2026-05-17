package com.example.zero.healthcare.dto.journal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreConditionDto {

    @NotNull @Min(1) @Max(10)
    private Integer jointMusclePain;

    @NotNull @Min(1) @Max(10)
    private Integer sleepHours;

    @NotNull @Min(1) @Max(10)
    private Integer sleepQuality;

    @NotNull @Min(1) @Max(10)
    private Integer previousFatigue;

    @NotNull @Min(1) @Max(10)
    private Integer overallCondition;
}
