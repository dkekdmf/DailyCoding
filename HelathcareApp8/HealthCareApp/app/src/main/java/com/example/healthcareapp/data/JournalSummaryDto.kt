package com.example.healthcareapp.data


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
    val conditionScores: List<Float> = listOf(10f, 10f, 10f, 10f, 10f),
    val conditionMemo: String = "",
    val painTag: String = "기록된 통증이 없습니다",

    // 🎯 [신규 추가] 업로드된 이미지를 안전하게 담아둘 Base64 스트링 저장 방 개설!
    val imageString: String = "" // 기본값은 빈 문자열 처리
) : java.io.Serializable