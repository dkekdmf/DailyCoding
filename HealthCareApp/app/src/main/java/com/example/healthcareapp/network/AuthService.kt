package com.example.healthcareapp.network

import com.example.healthcareapp.data.GoogleLoginRequest
import com.example.healthcareapp.data.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/api/auth/google")
    fun RegisterUser(@Body request:GoogleLoginRequest): Call<UserResponse>
}