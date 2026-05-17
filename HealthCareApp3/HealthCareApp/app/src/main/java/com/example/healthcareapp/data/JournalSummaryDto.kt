package com.example.healthcareapp.data

import com.google.gson.annotations.SerializedName

data class JournalSummaryDto(
    @SerializedName("journalId") // 🚨 서버가 주는 "journalId"라는 키를 안드로이드의 id에 쏙 매핑합니다!
    val id: Long,
    val workoutDate: String,
    val workoutType: String?,
    val title: String?
)