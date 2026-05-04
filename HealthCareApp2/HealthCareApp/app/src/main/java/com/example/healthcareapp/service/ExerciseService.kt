package com.example.healthcareapp.service

import com.example.healthcareapp.data.ExerciseItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ExerciseService {
    @GET("exercises")
    fun getAllExercises(
        @Header("x-rapidapi-key") apiKey: String,
        @Header("x-rapidapi-host") host: String = "exercisedb.p.rapidapi.com"
    ): Call<List<ExerciseItem>>
}