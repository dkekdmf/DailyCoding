package com.example.zero.healthcare.dto.folder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InviteAcceptRequest {
    private Long folderId;
    private String token;
}
