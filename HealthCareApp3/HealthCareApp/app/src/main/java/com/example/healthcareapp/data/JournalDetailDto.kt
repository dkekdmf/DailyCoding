package com.example.healthcareapp.data

data class JournalDetailDto(
    val journalId: Long,
    val folderId: Long,
    val workoutDate: String,
    val createdAt: String,
    val startedAt: String,
    val totalDurationSeconds: Int?,
    val preCondition: PreConditionResponseDto?,
    val postCondition: PostConditionResponseDto?,
    val content: String?,
    val painRecords: List<PainRecordResponseDto>,
    val exercises: List<ExerciseResponseDto>
)
// 컨디션 응답용
data class PreConditionResponseDto(
    val jointMusclePain: Int,
    val sleepHours: Int,
    val sleepQuality: Int,
    val previousFatigue: Int,
    val overallCondition: Int
)

// 통증 기록 응답용
data class PainRecordResponseDto(
    val id: Long,
    val part: String,
    val intensity: Int
)

// 운동 기록 응답용 (필요시)
data class ExerciseResponseDto(
    val id: Long,
    val name: String,
    val sets: List<SetResponseDto>
)
// 1. 운동 후 컨디션 응답 (JournalDetailDto에서 사용)
data class PostConditionResponseDto(
    val jointMusclePain: Int,
    val overallCondition: Int
)

// 2. 운동 세트 정보 (ExerciseResponseDto에서 사용)
data class SetResponseDto(
    val id: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean
)
data class ExerciseRequestDto(
    val exerciseId: Long, // 어떤 운동인지 (예: 스쿼트 ID)
    val sets: List<ExerciseSetRequestDto> = emptyList() // 세트 정보들
)
data class ExerciseSetRequestDto(
    val setNumber: Int,     // 1세트, 2세트...
    val weight: Double,     // 무게
    val reps: Int,          // 횟수
    val isCompleted: Boolean = true // 완료 여부
)
