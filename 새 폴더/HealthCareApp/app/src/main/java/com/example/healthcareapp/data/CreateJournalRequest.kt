package com.example.healthcareapp.data

import com.google.gson.annotations.SerializedName

/**
 * 1. 일지 생성 요청 DTO
 */
data class CreateJournalRequest(
    val folderId: Long?, // 명시적으로 Long 지정
    val workoutDate: String,
    val startedAt: String,
    val preCondition: PreConditionDto,
    val painRecords: List<PainRecordDto> = emptyList() // 기본값으로 빈 리스트 강제 지정
)
data class PreConditionDto(
    @SerializedName("pre_joint_muscle_pain") val jointMusclePain: Int,
    @SerializedName("pre_sleep_hours") val sleepHours: Int,
    @SerializedName("pre_sleep_quality") val sleepQuality: Int,
    @SerializedName("pre_previous_fatigue") val previousFatigue: Int,
    @SerializedName("pre_overall_condition") val overallCondition: Int
)

data class PainRecordDto(
    val bodyPart: String,
    val side: String,
    val painLevel: Int
)

/**
 * 4. 서버 생성 성공 응답 DTO
 */
data class CreateJournalResponse(
    val id: Long,
    val createdAt: String
)