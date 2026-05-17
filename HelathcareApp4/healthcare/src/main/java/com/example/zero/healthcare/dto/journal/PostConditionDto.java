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
public class PostConditionDto {

    @NotNull @Min(1) @Max(10)
    private Integer jointMusclePain;

    @NotNull @Min(1) @Max(10)
    private Integer intensityFit;

    @NotNull @Min(1) @Max(10)
    private Integer goalAchieved;

    @NotNull @Min(1) @Max(10)
    private Integer dizziness;

    @NotNull @Min(1) @Max(10)
    private Integer mood;
}
