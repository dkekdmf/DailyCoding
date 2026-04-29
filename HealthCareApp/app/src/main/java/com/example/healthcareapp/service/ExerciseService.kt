package com.example.healthcareapp.service

import com.example.healthcareapp.data.ExerciseItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
// ExerciseService.kt
interface ExerciseService {
    @GET("exercises") // 끝에 /를 붙이지 마세요!
    fun getAllExercises(
        @Header("x-rapidapi-key") apiKey: String,
        @Header("x-rapidapi-host") host: String = "exercisedb.p.rapidapi.com"
    ): Call<List<ExerciseItem>>
}