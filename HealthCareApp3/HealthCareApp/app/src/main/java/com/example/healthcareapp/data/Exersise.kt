package com.example.healthcareapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val target: String,
    val gifUrl: String,
    val description: String,
    var isSelected: Boolean = false
) : Parcelable