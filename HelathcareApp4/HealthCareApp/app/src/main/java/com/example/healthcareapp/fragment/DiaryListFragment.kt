package com.example.healthcareapp.fragment

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.DiaryPrefsManager
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.WorkoutExerciseActivity
import com.example.healthcareapp.WorkoutFinishActivity
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DayItem
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.DateUtils
import com.example.healthcareapp.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.*

class DiaryListFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvDiaryList: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView
    private lateinit var fabStartWorkout: View
    private lateinit var tvFolderName: TextView
    private var tvDiaryTitle: TextView? = null

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button
    private lateinit var btnFloatingPause: ImageView

    private var currentFilterType = "최근순"
    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter
    private var folderId: Long = -1L
    private var currentCalendar = Calendar.getInstance()
    private var currentDaysList = mutableListOf<DayItem>()
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    private val masterDiaryList = mutableListOf<DiaryItem>()
    private val displayList = ArrayList<DiaryItem>()

    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private var isWorkoutCompletedToday = false
    private var folderName: String? = null
    private var isSharedMode = false
    private val workoutResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            // 1. 데이터 추출
            val selectedEmojiResId = data.getIntExtra("EMOJI_RES_ID", emojiList[0])
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

            // 2. 리스트에 먼저 추가 (중요!)
            val newDiary = DiaryItem(
                System.currentTimeMillis().toString(),
                fullDate,
                "개인운동",
                selectedEmojiResId
            )
            masterDiaryList.add(0, newDiary)
            isWorkoutCompletedToday = true

            // 3. 필터 적용 후 캘린더 갱신
            applyFilterAndSort(currentFilterType) // 리스트 먼저 갱신
            setupCalendar()                       // 리스트를 참조해서 캘린더 갱신

            // 4. UI 강제 리프레시
            dayAdapter.notifyDataSetChanged()
            rvDiaryList.scrollToPosition(0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.folder_detial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            folderId = it.getLong("FOLDER_ID", -1L)
            folderName = it.getString("FOLDER_NAME")
            isSharedMode = it.getBoolean("IS_SHARED_MODE", false)
        }
        initViews(view)
        setupModeUI()
        initAdapters()
        setupCalendar()
        setupDiaryList()
        setupFloatingTimerObserver()
        initClickListeners(view)
    }

    private fun initViews(view: View) {
        rvCalendar = view.findViewById(R.id.rv_calendar)
        rvDiaryList = view.findViewById(R.id.rv_diary_list)
        tvWeekTitle = view.findViewById(R.id.tv_week_title)
        btnPrevWeek = view.findViewById(R.id.btn_prev_week)
        btnNextWeek = view.findViewById(R.id.btn_next_week)
        fabStartWorkout = view.findViewById(R.id.fab_start_workout)
        tvFolderName = view.findViewById(R.id.tv_folder_name)
        tvDiaryTitle = view.findViewById(R.id.tv_diary_title)
        layoutFloatingTimer = view.findViewById(R.id.layout_floating_timer)
        tvFloatingTimer = layoutFloatingTimer.findViewById(R.id.tv_bar_time)
        btnFloatingFinish = layoutFloatingTimer.findViewById(R.id.btn_bar_finish)
        btnFloatingPause = view.findViewById(R.id.btn_bar_pause)
    }

    private fun setupModeUI() {
        tvFolderName.text = folderName ?: "일지"
        tvDiaryTitle?.text = if (isSharedMode) "공유 일지" else "나의 일지"
    }

    private fun initAdapters() {
        dayAdapter = DayAdapter(emptyList()) { clickedItem ->
            selectedDateStr = clickedItem.fullDate
        }
        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        // ⭐ 2. 일지 클릭 시 상세 화면으로 데이터 전달 (EMOJI_RES_ID 포함)
        diaryAdapter = DiaryAdapter(displayList, { item ->
            Log.d("JaehoonTest", "상세화면 이동 - 보낼 색상 ID: ${item.emojiResId}")
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("SELECT_TAB", 1)
                putExtra("DIARY_DATE", item.date)
                putExtra("EMOJI_RES_ID", item.emojiResId) // ✅ 저장된 색상 그대로 전달
                putExtra("DIARY_ID", item.id)
            }
            startActivity(intent)
        }, { position -> })

        rvDiaryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diaryAdapter
        }
    }

    private fun applyFilterAndSort(filterType: String) {
        currentFilterType = filterType
        updateFilterUI(filterType)
        var filteredList = masterDiaryList.toList()
        filteredList = when (filterType) {
            "최근순", "운동한 날만" -> filteredList.sortedByDescending { it.date }
            "오래된순" -> filteredList.sortedBy { it.date }
            else -> filteredList
        }
        displayList.clear()
        displayList.addAll(filteredList)
        diaryAdapter.notifyDataSetChanged()
    }

    private fun setupDiaryList() {
        masterDiaryList.clear()
        // 🎯 [수정] SharedPreferences에서 저장된 일지들을 불러와서 리스트에 채워줍니다.
        val savedJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
        val savedItems = savedJournals.map {
            DiaryItem(it.id.toString(), it.createdAt.take(10), it.workoutType, it.emojiResId)
        }
        masterDiaryList.addAll(savedItems)

        applyFilterAndSort("최근순")
    }

    // ⭐ 3. 캘린더 그리면서 일지와 색상 맞추기
    private fun setupCalendar() {
        // 1. 현재 날짜(currentCalendar)를 기준으로 주 정보를 가져옵니다.
        val (title, rawDays) = DateUtils.getWeekInfo(currentCalendar.time)

        // ✅ [핵심 추가] 상단 타이틀(예: 2026.05 2주차)을 현재 주에 맞게 업데이트합니다.
        tvWeekTitle.text = title

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        rawDays.forEach { day ->
            // 선택된 날짜 표시 유지
            day.isSelected = (day.fullDate == selectedDateStr)

            if (day.fullDate == todayStr && isWorkoutCompletedToday) {
                // 리스트에서 오늘 날짜의 일지를 찾아서 그 색상을 가져옴
                val todayLog = masterDiaryList.find { it.date == todayStr }

                if (todayLog != null) {
                    day.hasExercise = true
                    day.emojiResId = todayLog.emojiResId
                }
            }
        }

        // 어댑터에 데이터 전달 (currentDaysList 업데이트)
        currentDaysList = rawDays.toMutableList()
        dayAdapter.updateData(currentDaysList)
    }

    private fun initClickListeners(view: View) {
        view.findViewById<ImageView>(R.id.arrow_btn).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }
        view.findViewById<TextView>(R.id.tv_sort_latest)?.setOnClickListener { applyFilterAndSort("최근순") }
        view.findViewById<TextView>(R.id.tv_sort_oldest)?.setOnClickListener { applyFilterAndSort("오래된순") }
        view.findViewById<View>(R.id.cb_only_workout)?.setOnClickListener {
            val nextFilter = if (currentFilterType == "운동한 날만") "최근순" else "운동한 날만"
            applyFilterAndSort(nextFilter)
        }
        fabStartWorkout.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                putExtra("IS_SHARED_MODE", isSharedMode)
                putExtra("FOLDER_ID", folderId) // 🎯 반드시 현재 폴더 ID를 넘겨야 합니다!
                putExtra("FOLDER_NAME", folderName)
            }
            workoutResultLauncher.launch(intent)
        }
        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        btnFloatingPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                btnFloatingPause.setImageResource(R.drawable.play)
            } else {
                TimerManager.startTimer()
                btnFloatingPause.setImageResource(R.drawable.pause)
            }
        }
    }

    private fun updateFilterUI(filterType: String) {
        val v = view ?: return
        val tvLatest = v.findViewById<TextView>(R.id.tv_sort_latest)
        val tvOldest = v.findViewById<TextView>(R.id.tv_sort_oldest)
        val ivCheck = v.findViewById<ImageView>(R.id.cb_only_workout)
        val colorActive = Color.parseColor("#000000")
        val colorInactive = Color.parseColor("#94A3B8")
        tvLatest?.setTextColor(if (filterType == "최근순") colorActive else colorInactive)
        tvOldest?.setTextColor(if (filterType == "오래된순") colorActive else colorInactive)
        ivCheck?.setImageResource(if (filterType == "운동한 날만") R.drawable.checkbox3 else R.drawable.checkbox1)
    }

    private fun setupFloatingTimerObserver() {
        // 1. ⏱️ 타이머 데이터 및 상태 관찰
        TimerManager.timeLiveData.observe(viewLifecycleOwner) { time ->
            val isTimerActive = TimerManager.isTimerActive()
            // 🎯 [수정] 현재 실행 여부를 직접 가져와서 체크합니다.
            val isRunning = TimerManager.isRunning()

            if (time > 0 && isTimerActive) {
                layoutFloatingTimer.visibility = View.VISIBLE
                tvFloatingTimer.text = TimerManager.getFormattedTime()
                fabStartWorkout.visibility = View.GONE

                // 🎯 [아이콘 전환] 현재 Running 상태에 따라 실시간 아이콘 변경
                if (isRunning) {
                    btnFloatingPause.setImageResource(R.drawable.pause) // ⏸️ 아이콘
                } else {
                    btnFloatingPause.setImageResource(R.drawable.play)  // ▶️ 아이콘
                }
            } else {
                // 🎯 WorkoutExerciseActivity에서 stopAndResetTimer() 호출 시 이쪽으로 들어와 즉시 숨겨짐
                layoutFloatingTimer.visibility = View.GONE
                fabStartWorkout.visibility = View.VISIBLE
            }
        }

        // 2. 🎯 [화면 복귀] 타이머 바 클릭 시 진행 중인 운동 화면으로 이동
        layoutFloatingTimer.setOnClickListener {
            Log.d("JaehoonLog", "⏰ 타이머 바 클릭: 운동 화면(WorkoutExerciseActivity)으로 복귀")

            val intent = Intent(requireContext(), com.example.healthcareapp.WorkoutExerciseActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
                putExtra("IS_SHARED_MODE", isSharedMode)
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            startActivity(intent)
        }

        // 3. 🎯 [제어] 일시정지/재개 버튼 클릭 리스너
        btnFloatingPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                Log.d("JaehoonLog", "⏸️ 타이머 일시정지")
            } else {
                TimerManager.startTimer()
                Log.d("JaehoonLog", "▶️ 타이머 재개")
            }
            // 💡 [수정] 버튼 클릭 시 LiveData에 현재 시간 값을 다시 한번 쏴줘서 UI 옵저버가 아이콘을 바꾸게 유도합니다.
            TimerManager.timeLiveData.value = TimerManager.timeLiveData.value
        }

        // 4. 운동 완료 버튼
        btnFloatingFinish.setOnClickListener {
            showFinishDialog()
        }
    }
    private fun showFinishDialog() {
        val totalTime = TimerManager.getFormattedTime()
        val endTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())
        val startTime = "16:16" // 나중에 실제 시작시간으로 변경 권장

        val dialog = WorkoutFinishDialog {
            val intent = Intent(requireContext(), WorkoutFinishActivity::class.java).apply {
                putExtra("TOTAL_TIME", totalTime)
                putExtra("START_TIME", startTime)
                putExtra("END_TIME", endTime)
                putExtra("IS_SHARED_MODE", isSharedMode)
                // 🎯 여기서도 folderId를 넘겨줘야 최종 저장이 가능합니다.
                putExtra("FOLDER_ID", folderId)
            }
            workoutResultLauncher.launch(intent)
        }
        dialog.show(parentFragmentManager, "WorkoutFinishDialog")
    }


    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}