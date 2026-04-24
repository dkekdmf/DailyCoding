package com.example.healthcareapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DiaryItem
import java.util.Calendar

class DiaryListActivty : AppCompatActivity() {

    private val diaryData = mutableListOf<DiaryItem>(

    )
    private lateinit var diaryAdapter: DiaryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary)

        val recyclerview = findViewById<RecyclerView>(R.id.rv_diary_list)
        recyclerview.layoutManager = LinearLayoutManager(this)

        // 어댑터 연결 (클릭 리스너 포함)
        diaryAdapter = DiaryAdapter(diaryData) { item ->
            val intent = Intent(this, DiaryDetailActivity::class.java)
            intent.putExtra("diaryId", item.id)
            startActivity(intent)
        }
        recyclerview.adapter = diaryAdapter

        // 3. 주 단위 캘린더 영역 클릭
//        findViewById<View>(R.id.card_calendar).setOnClickListener {
//            Toast.makeText(this, "주 단위 캘린더를 엽니다.", Toast.LENGTH_SHORT).show()
//        }

        // 4. 일지 추가 버튼 클릭
        findViewById<Button>(R.id.btn_add_diary).setOnClickListener {
            val editText = EditText(this)
            addNewDiary()
        }

        // 5. 운동 시작 버튼
//        findViewById<Button>(R.id.btn_start_workout).setOnClickListener {
//
//        }


    }


    private fun addNewDiary() {
        // 1. 입력창(EditText) 만들기
        val editText = EditText(this)
        editText.hint = "일지 제목을 입력하세요"

        // 2. 다이얼로그 띄우기
        AlertDialog.Builder(this)
            .setTitle("새 일지 추가")
            .setView(editText)
            .setPositiveButton("생성") { _, _ ->
                val folderName = editText.text.toString()

                // 입력값이 비어있지 않을 때만 추가
                if (folderName.isNotEmpty()) {
                    val newId = (diaryData.size + 1).toString()


                    val newItem = DiaryItem(newId, "26.04.16", folderName, "이재훈")


                    diaryData.add(0, newItem)

                    // 어댑터에게 새로고침 알림
                    diaryAdapter.notifyItemInserted(0)

                    // 추가된 위치로 스크롤 이동
                    findViewById<RecyclerView>(R.id.rv_diary_list).scrollToPosition(0)
                } else {
                    Toast.makeText(this, "제목을 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
}}