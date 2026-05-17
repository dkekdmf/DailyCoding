package com.example.zero.healthcare.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary_folder_member",
        indexes = {
                @Index(name = "idx_folder_member_user_active", columnList = "user_id, left_at, folder_id")
        })
@Getter
@NoArgsConstructor
public class DiaryFolderMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private DiaryFolder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public static DiaryFolderMember join(DiaryFolder folder, User user) {
        DiaryFolderMember member = new DiaryFolderMember();
        member.folder = folder;
        member.user = user;
        member.joinedAt = LocalDateTime.now();
        member.leftAt = null;
        return member;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        this.leftAt = null;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.leftAt == null;
    }
}
