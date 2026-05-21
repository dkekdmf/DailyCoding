package com.example.healthcareapp.data


data class DiaryItem(
    val id: String,
    val date: String,
    val title: String,
    val emojiResId: Int,
    val imageString: String,
    val folderId: Long // 👈 이 필드가 꼭 있어야 합니다!
)