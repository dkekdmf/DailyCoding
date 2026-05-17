package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.example.zero.healthcare.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryFolderMemberRepository extends JpaRepository<DiaryFolderMember, Long> {

    @Query("SELECT m FROM DiaryFolderMember m WHERE m.folder = :folder AND m.leftAt IS NULL")
    List<DiaryFolderMember> findActiveMembers(@Param("folder") DiaryFolder folder);

    @Query("SELECT COUNT(m) FROM DiaryFolderMember m WHERE m.folder = :folder AND m.leftAt IS NULL")
    long countActiveMembers(@Param("folder") DiaryFolder folder);

    @Query("SELECT m FROM DiaryFolderMember m WHERE m.folder = :folder AND m.user = :user")
    Optional<DiaryFolderMember> findByFolderAndUser(@Param("folder") DiaryFolder folder, @Param("user") User user);

    @Query("SELECT m FROM DiaryFolderMember m JOIN FETCH m.folder f WHERE m.user = :user AND m.leftAt IS NULL AND f.status = 'ACTIVE'")
    List<DiaryFolderMember> findActiveFolderMembersByUser(@Param("user") User user);
}
