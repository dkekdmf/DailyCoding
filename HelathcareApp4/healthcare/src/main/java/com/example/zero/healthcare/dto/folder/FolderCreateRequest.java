package com.example.zero.healthcare.dto.folder;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FolderCreateRequest {
    @NotBlank
    private String name;
}
