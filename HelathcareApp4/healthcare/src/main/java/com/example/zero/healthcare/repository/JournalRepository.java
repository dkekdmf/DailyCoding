package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalRepository extends JpaRepository<WorkoutJournal, Long> {
    List<WorkoutJournal> findByAuthorId(Long authorId);
    List<WorkoutJournal> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
