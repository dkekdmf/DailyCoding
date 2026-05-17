package com.example.zero.healthcare.dto.folder;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FolderCreateResponse {
    private final Long folderId;
    private final String name;
    @JsonProperty("isShared")
    private final boolean isShared;
    private final LocalDateTime createdAt;

    public FolderCreateResponse(DiaryFolder folder) {
        this.folderId = folder.getId();
        this.name = folder.getName();
        this.isShared = false;
        this.createdAt = folder.getCreatedAt();
    }
}
