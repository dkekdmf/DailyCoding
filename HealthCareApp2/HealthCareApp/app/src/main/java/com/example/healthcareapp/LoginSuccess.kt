package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class LoginSuccess : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signupcomplete)

        val StartButton = findViewById<Button>(R.id.btn_start)

        StartButton.setOnClickListener{
            //val intent = Intent(this,FolderActivity::class.java)
            val intent = Intent(this,WorkoutActivity::class.java)
            startActivity(intent)
            finish()
        }
    }




}