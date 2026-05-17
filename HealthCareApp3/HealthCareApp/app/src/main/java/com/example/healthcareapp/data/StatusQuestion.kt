package com.example.healthcareapp.data

// StatusQuestion.kt
data class StatusQuestion(
    val id: Int,
    val title: String,
    val minLabel: String,
    val maxLabel: String,
    var score: Int = 1, // 슬라이더 기본값
    val guides: Map<Int, String> // 점수별 가이드 문구
)