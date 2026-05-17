package com.example.healthcareapp.data

// JournalSummaryDto.kt
data class JournalSummaryDto(
    val id: Long = System.currentTimeMillis(), // 🎯 id 필드 추가 (기본값으로 현재 시간 부여)
    val folderId: Long,
    val workoutType: String,
    val createdAt: String,
    val totalTime: String,
    val memo: String,
    val condition: String = "GOOD",
    val emojiResId: Int
    // 만약 condition 에러가 계속 난다면 여기에 추가: val condition: String = "GOOD"
)


