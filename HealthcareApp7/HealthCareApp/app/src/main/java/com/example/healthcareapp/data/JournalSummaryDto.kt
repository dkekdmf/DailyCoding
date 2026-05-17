package com.example.healthcareapp.data

// JournalSummaryDto.kt
data class JournalSummaryDto(
    val id: Long = System.currentTimeMillis(),
    val folderId: Long,
    val workoutType: String,
    val createdAt: String,
    val totalTime: String,
    val memo: String,
    val condition: String = "GOOD",
    val emojiResId: Int,
    val startTime: String = "00:00",
    val endTime: String = "00:00",
    val exerciseList: List<ExerciseRecord> = emptyList(),

    // 🎯 [신규 추가] 컨디션체크 5개 슬라이더 점수와 전용 메모/통증 태그 저장 방 개설
    val conditionScores: List<Float> = listOf(10f, 10f, 10f, 10f, 10f), // 기본값 10점 세팅
    val conditionMemo: String = "",
    val painTag: String = "기록된 통증이 없습니다"
) : java.io.Serializable