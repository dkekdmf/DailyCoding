package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.utils.DateUtils
import java.util.Calendar

class DiaryActivity : AppCompatActivity() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView
    private lateinit var exersizeStart: TextView
    private lateinit var conditionButton: TextView
    private lateinit var tvFolderTitle: TextView // 상단 일지 제목 표시용 (나의 일지/공유 일지)

    private var folderId: Long = -1L
    private var folderName: String? = null
    private lateinit var dayAdapter: DayAdapter
    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendarbar)

        // 1. Intent 데이터 받기
        handleIntent(intent)

        // 2. 뷰 초기화
        initViews()

        // 3. 캘린더 설정
        setupCalendar()

        // 4. 리스너 설정
        initClickListeners()
    }

    // ⭐ 컨디션 체크 후 돌아올 때 데이터를 새로고침하기 위해 필요
    override fun onResume() {
        super.onResume()
        refreshData()
    }

    // ⭐ FLAG_ACTIVITY_SINGLE_TOP 등으로 다시 호출될 때 데이터 갱신
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        refreshData()
    }

    private fun handleIntent(intent: Intent?) {
        folderId = intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        folderName = intent?.getStringExtra("FOLDER_NAME")
        Log.d("JaehoonLog", "현재 폴더: $folderName (ID: $folderId)")
    }

    private fun initViews() {
        tvWeekTitle = findViewById(R.id.tv_week_title)
        rvCalendar = findViewById(R.id.rv_calendar)
        btnPrevWeek = findViewById(R.id.btn_prev_week)
        btnNextWeek = findViewById(R.id.btn_next_week)
        exersizeStart = findViewById(R.id.exercise_start)
        conditionButton = findViewById(R.id.condition_check)

        // 상단 타이틀 텍스트뷰가 있다면 연결 (예: 나의 일지/공유 일지 구분용)
        // tvFolderTitle = findViewById(R.id.tv_folder_title)
        // tvFolderTitle.text = folderName ?: "나의 일지"
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        dayAdapter = DayAdapter(days) { clickedDay ->
            updateDiaryList(clickedDay.fullDate)
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(this@DiaryActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
        }
    }

    private fun initClickListeners() {
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // 컨디션 체크 버튼 (ConditionCheckActivity로 이동하도록 수정됨)
        conditionButton.setOnClickListener {
            Log.d("JaehoonLog", "컨디션 체크 이동")
            val intent = Intent(this, ConditionCheckActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
                putExtra("FOLDER_NAME", folderName)
            }
            startActivity(intent)
        }

        // 운동 시작 버튼
        exersizeStart.setOnClickListener {
            if (folderId == -1L) {
                Toast.makeText(this, "폴더 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, DiaryListActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
                putExtra("FOLDER_NAME", folderName)
            }
            startActivity(intent)
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title
        dayAdapter.updateData(days)
    }

    // 데이터를 새로고침하는 함수
    private fun refreshData() {
        // 여기서 서버 API를 호출하여 해당 날짜의 일지가 있는지 다시 확인해야 함
        // 예: loadDiaryFromApi(currentDate)
        Log.d("JaehoonLog", "화면 새로고침 실행: $folderName")

        // 캘린더 데이터 갱신 (필요 시)
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        dayAdapter.updateData(days)
    }

    private fun updateDiaryList(date: String) {
        // 날짜 클릭 시 해당 날짜의 운동 기록 리스트를 서버에서 가져오는 로직 필요
        Log.d("JaehoonLog", "선택된 날짜: $date")
    }
}