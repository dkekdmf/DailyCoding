package com.example.healthcareapp.data

data class ConditionRecord(
    var title: String,
    val questions: List<StatusQuestion1>, // 여기서 1을 붙여줘야 합니다!
    var memo: String = "",
    var score: Float = 0f,
    var painTag: String = "",
    var isExpanded: Boolean = false,
    var isShowAllQuestions: Boolean = false
)