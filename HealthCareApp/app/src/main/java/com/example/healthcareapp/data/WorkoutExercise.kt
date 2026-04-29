package com.example.healthcareapp.data

data class WorkoutExercise(
    val id: Int,
    val name: String,
    val sets: MutableList<WorkoutSet> = mutableListOf(
        WorkoutSet(1, 60, 10),
        WorkoutSet(2, 60, 10),
        WorkoutSet(3, 60, 10)
    )
)

data class WorkoutSet(
    var setNum: Int,
    var weight: Int,
    var reps: Int
)