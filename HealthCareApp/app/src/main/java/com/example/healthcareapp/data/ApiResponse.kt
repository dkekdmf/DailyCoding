package com.example.healthcareapp.data

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
