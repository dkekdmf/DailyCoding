package com.example.healthcareapp.data

data class ExerciseRecord(
    val id: Int,
    val name: String,
    val sets: MutableList<ExerciseSet>
)

data class ExerciseSet(
    var setNumber: Int,
    var weight: Int,
    var reps: Int
)