package com.example.healthcareapp.data

// JournalApiResponse.kt
data class JournalApiResponse<T>(
    val status: String?,   // 서버의 "OK" 필드와 매칭
    val data: T?,          // 실제 CreateJournalResponse 데이터
    val message: String?
)