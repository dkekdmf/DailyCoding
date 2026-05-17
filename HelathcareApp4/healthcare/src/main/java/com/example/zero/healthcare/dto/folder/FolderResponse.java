package com.example.zero.healthcare.dto.folder;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class FolderResponse {
    private final Long folderId;
    private final String name;
    @JsonProperty("isShared")
    private final boolean isShared;
    private final int memberCount;
    private final List<FolderMemberResponse> members;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public FolderResponse(DiaryFolder folder, List<DiaryFolderMember> activeMembers) {
        this.folderId = folder.getId();
        this.name = folder.getName();
        this.memberCount = activeMembers.size();
        this.isShared = activeMembers.size() >= 2;
        this.members = activeMembers.stream()
                .map(m -> new FolderMemberResponse(m.getUser().getId(), m.getUser().getNickname()))
                .toList();
        this.createdAt = folder.getCreatedAt();
        this.updatedAt = folder.getUpdatedAt();
    }
}
