package com.example.healthcareapp.data

data class ConditionRecord(
    val title: String,
    val time: String,
    var isExpanded: Boolean = false,
    var painScore: Float = 0f,       // 슬라이더 값
    var memo: String = ""            // 메모 내용
)