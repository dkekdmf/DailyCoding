package com.example.healthcareapp

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class DiaryDetailActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_detail)

        val edtext = findViewById<EditText>(R.id.et_diary_content)
       // val diaryId = intent.getStringExtra("diaryId")
    }

}