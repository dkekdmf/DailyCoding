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

    // 🎯 [신규 추가] 상세 일지 화면에 시각값과 운동 세트 리스트를 뿌려주기 위한 필수 필드 설계!
    val startTime: String = "00:00",
    val endTime: String = "00:00",
    val exerciseList: List<ExerciseRecord> = emptyList() // 운동 종목 및 세트 데이터 통째로 포장
)