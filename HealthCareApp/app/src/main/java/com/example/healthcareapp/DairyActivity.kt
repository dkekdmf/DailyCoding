package com.example.healthcareapp

import WorkoutRecordFragment
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.utils.DateUtils

import java.util.Calendar

class DiaryActivity : AppCompatActivity() {

    // 뷰 객체들을 담을 변수 선언 (lateinit으로 나중에 초기화)
    private lateinit var rvCalendar: RecyclerView      // 상단 주간 날짜 리사이클러뷰
    private lateinit var tvWeekTitle: TextView       // 현재 몇 월인지 표시하는 텍스트
    private lateinit var btnPrevWeek: ImageView      // 이전 주로 이동하는 버튼
    private lateinit var btnNextWeek: ImageView      // 다음 주로 이동하는 버튼
    private lateinit var exersizeStart : TextView     // 운동 기록 화면으로 넘어가는 텍스트/버튼
    private var folderId: Long = -1L
    private var folderName: String? = null
    private lateinit var dayAdapter: DayAdapter       // 날짜를 표시할 어댑터
    private var currentCalendar = Calendar.getInstance() // 현재 날짜 정보를 가진 캘린더 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendarbar) // 캘린더바 레이아웃 연결
        folderId = intent.getLongExtra("FOLDER_ID", -1L)
        folderName = intent.getStringExtra("FOLDER_NAME")
        // 1. 뷰들을 ID와 연결하여 초기화
        initViews()

        // 2. 캘린더 리사이클러뷰와 어댑터 설정
        setupCalendar()

        // 3. 버튼들의 클릭 이벤트 설정
        initClickListeners()

    }

    // findViewById를 통해 XML의 뷰들을 연결하는 함수
    private fun initViews() {
        tvWeekTitle = findViewById(R.id.tv_week_title)
        rvCalendar = findViewById(R.id.rv_calendar)
        btnPrevWeek = findViewById(R.id.btn_prev_week)
        btnNextWeek = findViewById(R.id.btn_next_week)
        exersizeStart = findViewById(R.id.exercise_start)
    }

    // 캘린더 초기 세팅 함수
    private fun setupCalendar() {
        // DateUtils를 사용해 현재 날짜가 포함된 주의 타이틀(00월)과 날짜 리스트(7일분)를 가져옴
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title // 타이틀 텍스트 설정 (예: 4월)

        // 어댑터 생성 (날짜 클릭 시 해당 날짜의 일지를 업데이트하는 콜백 포함)
        dayAdapter = DayAdapter(days) { clickedDay ->
            updateDiaryList(clickedDay.fullDate) // 클릭한 날짜로 목록 업데이트 함수 호출
        }

        // 리사이클러뷰 설정
        rvCalendar.apply {
            // 가로 방향(HORIZONTAL)으로 아이템을 배치하도록 설정
            layoutManager = LinearLayoutManager(this@DiaryActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter // 연결할 어댑터 지정

            // 리사이클러뷰가 화면에 그려진 직후 데이터를 갱신하도록 예약
            post {
                dayAdapter.notifyDataSetChanged()
            }
        }
    }

    // 클릭 리스너들을 모아놓은 함수
    private fun initClickListeners() {
        // 이전 주 버튼 클릭 시 1주 전으로 이동
        btnPrevWeek.setOnClickListener {
            moveWeek(-1)
        }

        // 다음 주 버튼 클릭 시 1주 후로 이동
        btnNextWeek.setOnClickListener {
            moveWeek(1)
        }

        // '운동 기록 시작' 버튼 클릭 시 DiaryListActivity(일지 목록 화면)로 이동
        // '운동 기록 시작' 버튼 클릭 시 정보를 가지고 DiaryListActivity로 이동

        exersizeStart.setOnClickListener {
            // 목적지를 WorkoutActivity::class.java로 변경했습니다.
            val intent = Intent(this, WorkoutExerciseActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)      // 폴더 ID 전달
                putExtra("FOLDER_NAME", folderName)  // 폴더 이름 전달
            }
            startActivity(intent)
        }
    }

    // 주간 단위를 변경하는 함수 (offset: -1이면 이전 주, 1이면 다음 주)
    private fun moveWeek(offset: Int) {
        // 현재 캘린더 날짜에 7일(offset * 7)을 더하거나 뺌
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)

        // 바뀐 날짜 기준으로 새로운 주 정보를 다시 가져옴
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)

        // 뷰 텍스트 변경 및 어댑터 데이터 갱신
        tvWeekTitle.text = title
        dayAdapter.updateData(days) // 어댑터 내부에 구현된 데이터 교체 함수 호출
    }

    // 특정 날짜를 클릭했을 때 아래쪽 일지 리스트를 갱신하는 함수 (내용 구현 필요)
    private fun updateDiaryList(date: String) {

    }
}