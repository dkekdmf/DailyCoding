package com.example.healthcareapp

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DayItem
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.DateUtils
import com.example.healthcareapp.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 폴더 상세 화면: 주간 캘린더를 통해 날짜별 일지를 확인하고, 운동 시작/관리 기능을 제공
 */
class DiaryListActivity : AppCompatActivity() {

    // UI 컴포넌트 변수
    private lateinit var rvCalendar: RecyclerView      // 상단 가로 캘린더
    private lateinit var rvDiaryList: RecyclerView      // 하단 세로 일지 리스트
    private lateinit var tvWeekTitle: TextView          // 현재 표시 중인 달 (예: 2026.05)
    private lateinit var btnPrevWeek: ImageView         // 이전 주 이동 버튼
    private lateinit var btnNextWeek: ImageView         // 다음 주 이동 버튼
    private lateinit var fabStartWorkout: View          // 운동 시작 플로팅 버튼

    // 타이머가 작동 중일 때 나타나는 하단 바 UI
    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button

    // 리사이클러뷰 어댑터
    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter

    // 캘린더 상태 관리 변수
    private var currentCalendar = Calendar.getInstance() // 현재 보고 있는 기준 날짜
    private var currentDaysList = mutableListOf<DayItem>() // 현재 화면에 표시된 7일 데이터

    // ⭐ 현재 선택된 날짜 (yyyy-MM-dd): 주간 이동 시에도 선택 상태를 유지하기 위함
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    private val displayList = ArrayList<DiaryItem>() // 일지 리스트 데이터

    // 운동 완료 시 랜덤으로 찍어줄 이모티콘 리스트
    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private var isWorkoutCompletedToday = false // 오늘 운동 완료 여부 (임시 플래그)

    // [운동 화면] 결과 처리 Launcher: 운동을 마치고 돌아왔을 때 처리
    private val workoutResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isWorkoutCompletedToday = true
            markTodayWithEmoji() // 오늘 날짜에 이모티콘 표시
            TimerManager.stopTimer() // 타이머 종료
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder_detial)

        initViews()                 // 뷰 바인딩
        initAdapters()              // 어댑터 초기화 및 리사이클러뷰 연결
        setupCalendar()             // 캘린더 날짜 생성 및 세팅
        setupDiaryList()            // 일지 데이터 세팅
        setupFloatingTimerObserver() // 전역 타이머 상태 관찰 설정
        initClickListeners()        // 클릭 리스너 설정
    }

    private fun initViews() {
        rvCalendar = findViewById(R.id.rv_calendar)
        rvDiaryList = findViewById(R.id.rv_diary_list)
        tvWeekTitle = findViewById(R.id.tv_week_title)
        btnPrevWeek = findViewById(R.id.btn_prev_week)
        btnNextWeek = findViewById(R.id.btn_next_week)
        fabStartWorkout = findViewById(R.id.fab_start_workout)

        layoutFloatingTimer = findViewById(R.id.layout_floating_timer)
        tvFloatingTimer = layoutFloatingTimer.findViewById(R.id.tv_bar_time)
        btnFloatingFinish = layoutFloatingTimer.findViewById(R.id.btn_bar_finish)
    }

    private fun initAdapters() {
        // 날짜 클릭 시 동작 정의
        dayAdapter = DayAdapter(emptyList()) { clickedItem ->
            selectedDateStr = clickedItem.fullDate // 선택된 날짜 업데이트
            // TODO: 해당 날짜의 일지만 서버에서 가져오거나 필터링하는 로직 추가
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(this@DiaryListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter

            // ⭐ 버그 해결: 갱신 시 깜빡임 방지 및 성능 최적화
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    /**
     * 주간 캘린더 데이터를 생성하고 UI에 적용하는 함수
     */
    private fun setupCalendar() {
        // 1. DateUtils를 사용해 해당 주의 [년.월] 제목과 7일간의 [DayItem] 리스트를 가져옴
        val weekInfo = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = weekInfo.first

        val rawDays = weekInfo.second
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        rawDays.forEach { day ->
            // 2. 이전에 선택했던 날짜라면 선택 상태(isSelected)를 true로 복구
            day.isSelected = (day.fullDate == selectedDateStr)

            // 3. 오늘 날짜이고 운동을 완료했다면 이모티콘 정보 주입
            if (day.fullDate == todayStr && isWorkoutCompletedToday) {
                day.hasExercise = true
                if (day.emojiResId == 0) day.emojiResId = emojiList.random()
            }
        }

        currentDaysList = rawDays.toMutableList()
        dayAdapter.updateData(currentDaysList) // 어댑터 갱신
    }

    /**
     * 오늘 날짜에 운동 완료 이모티콘을 표시하고 리스트를 갱신
     */
    private fun markTodayWithEmoji() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        currentDaysList.forEach { day ->
            if (day.fullDate == todayStr) {
                day.hasExercise = true
                if (day.emojiResId == 0) day.emojiResId = emojiList.random()
            }
        }
        dayAdapter.notifyDataSetChanged()
    }

    /**
     * 타이머Manager의 LiveData를 관찰하여 하단 플로팅 바를 노출/숨김 처리
     */
    private fun setupFloatingTimerObserver() {
        TimerManager.timeLiveData.observe(this) {
            if (TimerManager.isTimerActive()) {
                // 타이머가 돌아가고 있으면 플로팅 바를 보여주고 시작 버튼을 숨김
                layoutFloatingTimer.visibility = View.VISIBLE
                tvFloatingTimer.text = TimerManager.getFormattedTime()
                fabStartWorkout.visibility = View.GONE
            } else {
                // 타이머가 종료되면 플로팅 바를 숨기고 시작 버튼을 노출
                layoutFloatingTimer.visibility = View.GONE
                fabStartWorkout.visibility = View.VISIBLE
            }
        }
        btnFloatingFinish.setOnClickListener { showFinishDialog() }
    }

    /**
     * 운동 종료 확인 다이얼로그 노출
     */
    private fun showFinishDialog() {
        val dialog = WorkoutFinishDialog {
            isWorkoutCompletedToday = true
            markTodayWithEmoji()
            TimerManager.stopTimer()
        }
        dialog.show(supportFragmentManager, "WorkoutFinishDialog")
    }

    /**
     * 하단 일지 리스트 더미 데이터 세팅
     */
    private fun setupDiaryList() {
        displayList.clear()
//        // TODO: 실제로는 서버에서 해당 폴더/날짜의 데이터를 받아와야 함
//        displayList.add(DiaryItem("26.04.10 qwer123", "개인운동"))
//        displayList.add(DiaryItem("26.04.09 asdf456", "PT"))
//        displayList.add(DiaryItem("26.04.08 qwer123", "개인운동"))
//        displayList.add(DiaryItem("26.04.07 asdf456", "PT"))

        rvDiaryList.layoutManager = LinearLayoutManager(this)
        diaryAdapter = DiaryAdapter(displayList, { item ->
            // 아이템 상세 클릭 시 처리
        }, { position ->
            // 점 세개(옵션) 버튼 클릭 시 처리
        })
        rvDiaryList.adapter = diaryAdapter
    }

    /**
     * 버튼 클릭 이벤트 리스너 통합 설정
     */
    private fun initClickListeners() {
        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.arrow_btn).setOnClickListener { finish() }

        // 주간 이동 버튼 (이전 주 / 다음 주)
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // 작동 중인 플로팅 타이머 바 클릭 시 현재 진행 중인 운동 화면으로 복귀
        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(this, WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 기존 스택의 액티비티를 앞으로 가져옴
            startActivity(intent)
        }

        // 새 운동 시작 버튼
        fabStartWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutExerciseActivity::class.java)
            workoutResultLauncher.launch(intent)
        }
    }

    /**
     * 기준 날짜(currentCalendar)를 offset 주만큼 이동시키고 캘린더 갱신
     */
    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}