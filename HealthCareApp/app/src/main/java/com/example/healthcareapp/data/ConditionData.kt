package com.example.healthcareapp.data

data class ConditionData(
    val title: String,    // 예: "운동 후 컨디션 체크"
    val time: String,     // 예: "14:06"
    val isCompleted: Boolean = true,
    var isExpanded: Boolean = false // 드롭다운 상태 저장
)
