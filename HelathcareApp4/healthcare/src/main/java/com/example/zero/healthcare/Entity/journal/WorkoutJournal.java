package com.example.zero.healthcare.Entity.journal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_journal")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE workout_journal SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkoutJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "pre_joint_muscle_pain", nullable = false)
    private Integer preJointMusclePain;

    @Column(name = "pre_sleep_hours", nullable = false)
    private Integer preSleepHours;

    @Column(name = "pre_sleep_quality", nullable = false)
    private Integer preSleepQuality;

    @Column(name = "pre_previous_fatigue", nullable = false)
    private Integer prePreviousFatigue;

    @Column(name = "pre_overall_condition", nullable = false)
    private Integer preOverallCondition;

    @Column(name = "post_joint_muscle_pain")
    private Integer postJointMusclePain;

    @Column(name = "post_intensity_fit")
    private Integer postIntensityFit;

    @Column(name = "post_goal_achieved")
    private Integer postGoalAchieved;

    @Column(name = "post_dizziness")
    private Integer postDizziness;

    @Column(name = "post_mood")
    private Integer postMood;

    @Column(name = "post_recorded_at")
    private LocalDateTime postRecordedAt;

    @Column(name = "content", length = 5000)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<JournalPainRecord> painRecords = new ArrayList<>();

    @Builder
    public WorkoutJournal(Long folderId, Long authorId,
                          Integer preJointMusclePain, Integer preSleepHours,
                          Integer preSleepQuality, Integer prePreviousFatigue,
                          Integer preOverallCondition) {
        this.folderId = folderId;
        this.authorId = authorId;
        this.preJointMusclePain = preJointMusclePain;
        this.preSleepHours = preSleepHours;
        this.preSleepQuality = preSleepQuality;
        this.prePreviousFatigue = prePreviousFatigue;
        this.preOverallCondition = preOverallCondition;
    }

    public void addPainRecord(JournalPainRecord record) {
        painRecords.add(record);
        record.setJournal(this);
    }

    public void applyPostCondition(Integer postJointMusclePain, Integer postIntensityFit,
                                   Integer postGoalAchieved, Integer postDizziness,
                                   Integer postMood, String content) {
        this.postJointMusclePain = postJointMusclePain;
        this.postIntensityFit = postIntensityFit;
        this.postGoalAchieved = postGoalAchieved;
        this.postDizziness = postDizziness;
        this.postMood = postMood;
        this.content = content;
        this.postRecordedAt = LocalDateTime.now();
    }
}
