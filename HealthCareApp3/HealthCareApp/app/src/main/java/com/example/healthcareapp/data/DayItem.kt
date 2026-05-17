package com.example.healthcareapp.data

import java.util.Date

data class DayItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val fullDate: String,
    val date: Date,           // ⭐ 이 필드가 없으면 weekList.add에서 에러가 납니다!
    var isSelected: Boolean = false,
    var hasExercise: Boolean = false,
    var emojiResId: Int = 0
)