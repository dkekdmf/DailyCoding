package com.example.healthcareapp.data

data class DayItem(
    val dayOfWeek: String, // 일, 월, 화 ..
    val dayNumber: String, // 5, 6, 7 ..
    val fullDate: String,  // 2026-04-05 ..
    var isSelected: Boolean = false
)