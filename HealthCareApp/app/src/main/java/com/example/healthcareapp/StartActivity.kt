package com.example.healthcareapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)

        val StartButton = findViewById<Button>(R.id.start_button)

        StartButton.setOnClickListener{
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)

        }
    }

}