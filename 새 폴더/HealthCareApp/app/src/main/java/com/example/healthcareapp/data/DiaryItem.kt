package com.example.healthcareapp.data


data class DiaryItem(
    val id: String,
    val date: String,
    val title: String,
    val emojiResId: Int // ⭐ 이모티콘 리소스 ID를 저장할 변수 추가
)