package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.compose.foundation.interaction.DragInteraction

class LoginSuccess : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signupcomplete)

        val StartButton = findViewById<Button>(R.id.btn_start)

        StartButton.setOnClickListener{
            //val intent = Intent(this,FolderActivity::class.java)
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }




}