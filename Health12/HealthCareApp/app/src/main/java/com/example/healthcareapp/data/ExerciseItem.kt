package com.example.healthcareapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
data class ExerciseItem(
    val id: String,
    val name: String,
    val bodyPart: String, // JSON의 키값이 "bodyPart"인 경우
    val gifUrl: String,
    var isSelected: Boolean = false,
    val target: String
)

