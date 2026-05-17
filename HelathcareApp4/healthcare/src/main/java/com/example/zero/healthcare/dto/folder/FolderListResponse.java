package com.example.zero.healthcare.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FolderListResponse {
    private List<FolderResponse> folders;
    private String nextCursor;
    private boolean hasNext;
}
