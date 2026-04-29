package com.example.healthcareapp.data

data class ExerciseRecord(
    val id: Long,
    val name: String,
    val sets: MutableList<ExerciseSet>
)

data class ExerciseSet(
    val setNumber: Int,
    var weight: Int,
    var reps: Int
)