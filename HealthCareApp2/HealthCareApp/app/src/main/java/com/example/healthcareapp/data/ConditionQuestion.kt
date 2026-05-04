package com.example.healthcareapp.data

data class ConditionQuestion(
    val id: Int,
    val question: String,
    val minLabel: String,
    val maxLabel: String,
    val guideTexts: List<String> // 0~10 단계별 가이드 텍스트
)