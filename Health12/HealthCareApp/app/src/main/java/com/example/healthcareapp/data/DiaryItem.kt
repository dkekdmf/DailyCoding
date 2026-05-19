package com.example.healthcareapp.data


data class DiaryItem(
    val id: String,
    val date: String,
    val title: String,
    val emojiResId: Int,
    val imageString: String = ""
)