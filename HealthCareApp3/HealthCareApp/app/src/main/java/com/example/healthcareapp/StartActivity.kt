package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)
        val splashScreen = installSplashScreen()
        val StartButton = findViewById<Button>(R.id.start_button)

        StartButton.setOnClickListener{
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)

        }
    }

}