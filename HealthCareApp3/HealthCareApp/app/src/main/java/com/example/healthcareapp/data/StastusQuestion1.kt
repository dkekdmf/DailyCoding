package com.example.healthcareapp.data

// StatusQuestion.kt 또는 데이터 정의 파일
data class StatusQuestion1(
    val step: String,      // "1/5" 같은 단계 텍스트
    val title: String,     // 질문 내용
    var score: Float = 0f, // 슬라이더 값 (반드시 Float)
    val minLabel: String = "매우 심함",
    val maxLabel: String = "통증 없음"
)