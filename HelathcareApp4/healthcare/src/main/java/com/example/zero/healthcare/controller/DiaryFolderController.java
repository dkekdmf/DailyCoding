package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.dto.folder.FolderCreateRequest;
import com.example.zero.healthcare.dto.folder.FolderCreateResponse;
import com.example.zero.healthcare.dto.folder.FolderListResponse;
import com.example.zero.healthcare.dto.folder.FolderResponse;
import com.example.zero.healthcare.dto.folder.FolderUpdateRequest;
import com.example.zero.healthcare.dto.folder.InviteAcceptRequest;
import com.example.zero.healthcare.dto.folder.InviteLinkResponse;
import com.example.zero.healthcare.service.DiaryFolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class DiaryFolderController {

    private final DiaryFolderService diaryFolderService;

    private Long currentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FolderListResponse>> listFolders(
            Authentication auth,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPDATED_AT") String sort,
            @RequestParam(required = false) Boolean shared) {
        return ResponseEntity.ok(ApiResponse.ok(diaryFolderService.listFolders(currentUserId(auth), cursor, size, sort, shared)));
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolder(Authentication auth, @PathVariable Long folderId) {
        return ResponseEntity.ok(ApiResponse.ok(diaryFolderService.getFolder(currentUserId(auth), folderId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FolderCreateResponse>> createFolder(Authentication auth, @Valid @RequestBody FolderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(diaryFolderService.createFolder(currentUserId(auth), request)));
    }

    @PatchMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(Authentication auth, @PathVariable Long folderId, @Valid @RequestBody FolderUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(diaryFolderService.updateFolderName(currentUserId(auth), folderId, request)));
    }

    @DeleteMapping("/{folderId}/members/me")
    public ResponseEntity<Void> leaveFolder(Authentication auth, @PathVariable Long folderId) {
        diaryFolderService.leaveFolder(currentUserId(auth), folderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{folderId}/invite-link")
    public ResponseEntity<ApiResponse<InviteLinkResponse>> createInviteLink(Authentication auth, @PathVariable Long folderId) {
        return ResponseEntity.ok(ApiResponse.ok(diaryFolderService.createInviteLink(currentUserId(auth), folderId)));
    }

    @PostMapping("/invite/accept")
    public ResponseEntity<ApiResponse<FolderResponse>> acceptInvite(Authentication auth, @RequestBody InviteAcceptRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(diaryFolderService.acceptInvite(currentUserId(auth), request)));
    }
}
