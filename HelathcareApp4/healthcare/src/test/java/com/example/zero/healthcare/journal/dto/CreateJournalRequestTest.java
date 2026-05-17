package com.example.zero.healthcare.journal.dto;

import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateJournalRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private PreConditionDto validPreCondition() {
        return new PreConditionDto(5, 7, 6, 4, 8);
    }

    private PainRecordDto painRecord(String bodyPart, String side, Integer painLevel) {
        return new PainRecordDto(bodyPart, side, painLevel);
    }

    @Test
    @DisplayName("userId가 null이면 violations가 발생한다")
    void validate_nullUserId_hasViolations() {
        CreateJournalRequest req = new CreateJournalRequest(null, null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
    }

    @Test
    @DisplayName("preCondition이 null이면 violations가 발생한다")
    void validate_nullPreCondition_hasViolations() {
        CreateJournalRequest req = new CreateJournalRequest(1L, null, null, null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("preCondition"));
    }

    @Test
    @DisplayName("preCondition 필드가 null이면 violations가 발생한다")
    void validate_preConditionFieldNull_hasViolations() {
        PreConditionDto pre = new PreConditionDto(null, 7, 6, 4, 8);
        CreateJournalRequest req = new CreateJournalRequest(1L, null, pre, null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("jointMusclePain"));
    }

    @Test
    @DisplayName("preCondition 필드가 1-10 범위 초과이면 violations가 발생한다")
    void validate_preConditionOutOfRange_hasViolations() {
        PreConditionDto pre = new PreConditionDto(11, 7, 6, 4, 8);
        CreateJournalRequest req = new CreateJournalRequest(1L, null, pre, null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("jointMusclePain"));
    }

    @Test
    @DisplayName("painRecords가 null이면 통과한다")
    void validate_nullPainRecords_noViolations() {
        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("painRecords가 빈 배열이면 통과한다")
    void validate_emptyPainRecords_noViolations() {
        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), List.of());
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("painRecords 내 bodyPart+side 중복이 있으면 violations가 발생한다")
    void validate_duplicatePainRecord_hasViolations() {
        List<PainRecordDto> records = List.of(
                painRecord("SHOULDER", "LEFT", 5),
                painRecord("SHOULDER", "LEFT", 7)
        );
        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), records);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("painRecordsUnique"));
    }

    @Test
    @DisplayName("painLevel이 null이면 통과한다 (PainRecordDto 필드 nullable)")
    void validate_painLevelNull_noViolations() {
        List<PainRecordDto> records = List.of(painRecord("SHOULDER", "LEFT", null));
        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), records);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("painLevel이 범위 초과이면 violations가 발생한다")
    void validate_painLevelOutOfRange_hasViolations() {
        List<PainRecordDto> records = List.of(painRecord("SHOULDER", "LEFT", 11));
        CreateJournalRequest req = new CreateJournalRequest(1L, null, validPreCondition(), records);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("painLevel"));
    }
}
