package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalPostRequest;
import com.example.zero.healthcare.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Journal", description = "운동 일지 관련 API")
@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @Operation(summary = "컨디션 기록 새 운동 일지 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateJournalResponse>> create(@Valid @RequestBody CreateJournalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(journalService.createJournal(request)));
    }

    @Operation(summary = "운동 후 기록 추가 (PATCH)")
    @PatchMapping("/{id}/post")
    public ResponseEntity<ApiResponse<CreateJournalResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJournalPostRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(journalService.updatePostCondition(id, request)));
    }

    @Operation(summary = "사용자 일지 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<JournalSummaryDto>>> getMyJournals(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(journalService.getMyJournals(userId)));
    }

    @Operation(summary = "일지 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalDetailDto>> getJournalDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(journalService.getJournalDetail(id)));
    }
}
