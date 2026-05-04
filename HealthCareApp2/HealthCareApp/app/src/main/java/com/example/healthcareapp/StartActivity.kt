package com.example.healthcareapp

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
            intent = Intent(this,WorkoutExerciseActivity::class.java)
            startActivity(intent)

        }
    }

}