package com.example.healthcareapp.data

import java.util.Date

// DayItem.kt
data class DayItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val fullDate: String,
    val date: Date, // 추가하셨던 date 필드
    var isSelected: Boolean = false,
    var hasExercise: Boolean = false,
    var emojiResId: Int = -1,
    val folderId: Long = -1L // 👈 = -1L(기본값)을 넣으세요!
)