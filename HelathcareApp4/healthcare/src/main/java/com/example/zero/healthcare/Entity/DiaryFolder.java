package com.example.zero.healthcare.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary_folder",
        indexes = {
                @Index(name = "idx_diary_folder_status", columnList = "status, updated_at")
        })
@Getter
@NoArgsConstructor
public class DiaryFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 18)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FolderStatus status = FolderStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum FolderStatus { ACTIVE, CLOSED }

    public static DiaryFolder create(String name) {
        DiaryFolder folder = new DiaryFolder();
        folder.name = name;
        folder.status = FolderStatus.ACTIVE;
        folder.createdAt = LocalDateTime.now();
        folder.updatedAt = LocalDateTime.now();
        return folder;
    }

    public void rename(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = FolderStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == FolderStatus.ACTIVE;
    }
}
