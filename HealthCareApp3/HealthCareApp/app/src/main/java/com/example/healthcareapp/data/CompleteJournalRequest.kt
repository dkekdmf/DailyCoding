package com.example.zero.healthcare.dto.journal

import com.example.healthcareapp.data.ExerciseRequestDto
import com.example.healthcareapp.data.PainRecordDto
import com.example.healthcareapp.data.PreConditionDto
import com.google.gson.annotations.SerializedName
data class CompleteJournalRequest(
    val workoutDate: String,            // yyyy-MM-dd
    val startedAt: String,              // yyyy-MM-ddTHH:mm:ss
    val totalDurationSeconds: Int? = 0,
    val folderId: Long?,
    val postCondition: PostConditionDto, // 🚨 핵심: 필수값!
    val painRecords: List<PainRecordDto> = emptyList(),
    val exercises: List<ExerciseDto> = emptyList(),
    val content: String? = "",
    val imageUrls: List<String> = emptyList()
)

data class PostConditionDto(
    val jointMusclePain: Int,
    val intensityFit: Int,
    val goalAchieved: Int,
    val dizziness: Int,
    val mood: Int
)

data class ExerciseDto(
    val exerciseName: String,
    val displayOrder: Int? = 1,
    val sets: List<SetDto> = emptyList()
)

data class SetDto(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double // 서버의 BigDecimal 대응
)