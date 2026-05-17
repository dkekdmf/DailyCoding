package com.example.healthcareapp.data

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken") val idToken : String,
    @SerializedName("email") val email : String,
    @SerializedName("nickname") val nickname : String




)
