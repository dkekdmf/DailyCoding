package com.example.zero.healthcare.dto.journal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PainRecordDto {

    private String bodyPart;

    private String side;

    @Schema(description = "통증 강도 (1~10)", example = "5")
    @Min(1) @Max(10)
    private Integer painLevel;
}
