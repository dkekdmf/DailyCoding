package com.example.healthcareapp.data

data class ConditionData(
    val title: String,
    val time: String,
    val isCompleted: Boolean = true,
    var isExpanded: Boolean = false
)
