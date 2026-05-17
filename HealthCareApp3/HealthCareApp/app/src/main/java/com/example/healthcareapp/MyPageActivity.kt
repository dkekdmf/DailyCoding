package com.example.healthcareapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyPageActivity : AppCompatActivity(){

    private lateinit var userEmail : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "이메일 없음")

        val getEmail = findViewById<TextView>(R.id.signedupemail)
        getEmail.text = userEmail



    }
}