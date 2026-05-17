package com.example.healthcareapp.fragment

import WorkoutFinishDialog
import android.app.Activity
import android.content.Context
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

    private val sharedPrefs by lazy {
        requireContext().getSharedPreferences("CalendarEmojiPrefs", Context.MODE_PRIVATE)
    }

    private val workoutResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val selectedEmojiResId = data.getIntExtra("EMOJI_RES_ID", emojiList[0])
            // 🎯 [정밀 교정] FinishActivity에서 최종 정산 통과된 모드(PT / 개인운동)를 실시간 동적 매칭 수신합니다.
            val resultWorkoutType = data.getStringExtra("WORKOUT_TYPE") ?: "개인운동"
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

            Log.d("JaehoonSync", "🎬 운동 완료 수신 -> 타입: $resultWorkoutType, 폴더ID: $folderId, 날짜: $fullDate")

            val folderIsolatedKey = "${fullDate}_$folderId"
            sharedPrefs.edit().putInt(folderIsolatedKey, selectedEmojiResId).apply()

            val newDiaryId = System.currentTimeMillis()

            try {
                val journalDto = com.example.healthcareapp.data.JournalSummaryDto(
                    id = newDiaryId,
                    folderId = folderId,
                    workoutType = resultWorkoutType, // 🎯 하드코딩 제거 후 연동
                    createdAt = fullDate,
                    totalTime = "00:00",
                    memo = "",
                    condition = "GOOD",
                    emojiResId = selectedEmojiResId
                )

                DiaryPrefsManager.saveJournal(requireContext(), folderId, journalDto)
                Log.d("JaehoonSync", "💾 DiaryPrefsManager 폴더별 DTO 파일 세이브 성공!")

            } catch (e: Exception) {
                Log.e("JaehoonSync", "저장소 저장 중 예외 발생: ${e.message}")
            }

            val newDiary = DiaryItem(
                newDiaryId.toString(),
                fullDate,
                resultWorkoutType, // 🎯 리스트 갱신 시에도 반영되도록 수정
                selectedEmojiResId
            )
            masterDiaryList.add(0, newDiary)
            isWorkoutCompletedToday = true

            applyFilterAndSort(currentFilterType)
            setupCalendar()

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
            setupCalendar()
        }
        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        diaryAdapter = DiaryAdapter(displayList, { item ->
            Log.d("JaehoonTest", "상세화면 이동 - 보낼 색상 ID: ${item.emojiResId}")
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("SELECT_TAB", 1)
                putExtra("DIARY_DATE", item.date)
                putExtra("EMOJI_RES_ID", item.emojiResId)
                putExtra("DIARY_ID", item.id)
                putExtra("FOLDER_ID", folderId)
            }
            startActivity(intent)
        }, { position ->
            Log.d("JaehoonExit", "일지 단건 삭제 요청 - 포지션: $position")
            showFolderExitSheet(position)
        })

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

        val savedJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)

        if (savedJournals.isNotEmpty()) {
            val sortedJournals = savedJournals.sortedByDescending { it.createdAt }

            val savedItems = sortedJournals.mapIndexed { index, journal ->
                val dateKey = journal.createdAt.take(10)
                val folderIsolatedKey = "${dateKey}_$folderId"

                if (index == 0) {
                    sharedPrefs.edit().putInt(folderIsolatedKey, journal.emojiResId).apply()
                } else {
                    if (sharedPrefs.getInt(folderIsolatedKey, -1) == -1) {
                        sharedPrefs.edit().putInt(folderIsolatedKey, journal.emojiResId).apply()
                    }
                }

                DiaryItem(
                    id = journal.id.toString(),
                    date = dateKey,
                    title = journal.workoutType, // 데이터 로드 시에도 PT / 개인운동 분기 유지
                    emojiResId = journal.emojiResId
                )
            }
            masterDiaryList.addAll(savedItems)
        }

        applyFilterAndSort(currentFilterType)
        setupCalendar()
    }

    private fun setupCalendar() {
        val (title, rawDays) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        rawDays.forEach { day ->
            day.isSelected = (day.fullDate == selectedDateStr)

            val folderIsolatedKey = "${day.fullDate}_$folderId"
            val savedEmojiId = sharedPrefs.getInt(folderIsolatedKey, -1)

            if (savedEmojiId != -1) {
                day.hasExercise = true
                day.emojiResId = savedEmojiId
            } else {
                val logForDay = masterDiaryList.find { it.date == day.fullDate }
                if (logForDay != null) {
                    day.hasExercise = true
                    day.emojiResId = logForDay.emojiResId
                    sharedPrefs.edit().putInt(folderIsolatedKey, logForDay.emojiResId).apply()
                } else {
                    day.hasExercise = false
                    day.emojiResId = -1
                }
            }
        }

        currentDaysList = rawDays.toMutableList()
        dayAdapter.updateData(currentDaysList)
        dayAdapter.notifyDataSetChanged()
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
                putExtra("FOLDER_ID", folderId)
                putExtra("FOLDER_NAME", folderName)
            }
            workoutResultLauncher.launch(intent)
        }
        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
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
        TimerManager.timeLiveData.observe(viewLifecycleOwner) { time ->
            val isTimerActive = TimerManager.isTimerActive()
            val isRunning = TimerManager.isRunning()

            if (time > 0 && isTimerActive) {
                layoutFloatingTimer.visibility = View.VISIBLE
                tvFloatingTimer.text = TimerManager.getFormattedTime()
                fabStartWorkout.visibility = View.GONE

                if (isRunning) {
                    btnFloatingPause.setImageResource(R.drawable.pause)
                } else {
                    btnFloatingPause.setImageResource(R.drawable.play)
                }
            } else {
                layoutFloatingTimer.visibility = View.GONE
                fabStartWorkout.visibility = View.VISIBLE
            }
        }

        btnFloatingPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
            } else {
                TimerManager.startTimer()
            }
            TimerManager.timeLiveData.value = TimerManager.timeLiveData.value
        }

        btnFloatingFinish.setOnClickListener {
            showFinishDialog()
        }
    }

    private fun showFinishDialog() {
        val totalTime = TimerManager.getFormattedTime()
        val endTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())
        val startTime = "16:16"

        val dialog = WorkoutFinishDialog {
            val intent = Intent(requireContext(), WorkoutFinishActivity::class.java).apply {
                putExtra("TOTAL_TIME", totalTime)
                putExtra("START_TIME", startTime)
                putExtra("END_TIME", endTime)
                putExtra("IS_SHARED_MODE", isSharedMode)
                putExtra("FOLDER_ID", folderId)
                // 🎯 [예외 처리] 플로팅 타이머에서 즉시 완료 버튼을 눌러 탈출할 때 기본 모드 세팅 방어선 구축
                putExtra("WORKOUT_TYPE", "개인운동")
            }
            workoutResultLauncher.launch(intent)
        }
        dialog.show(parentFragmentManager, "WorkoutFinishDialog")
    }

    private fun showFolderExitSheet(position: Int) {
        val targetItem = displayList.getOrNull(position) ?: return

        val exitSheet = com.example.healthcareapp.sheet.FolderExitSheet2(
            folderName = targetItem.title,
            onExitConfirm = {
                Log.d("JaehoonExit", "🚨 특정 일지 단건 삭제 수행 -> ID: ${targetItem.id}, 날짜: ${targetItem.date}")

                try {
                    val prefs = requireContext().getSharedPreferences("HealthDiaryPrefs", Context.MODE_PRIVATE)
                    val existingList = DiaryPrefsManager.getJournals(requireContext(), folderId).toMutableList()

                    val iterator = existingList.iterator()
                    while (iterator.hasNext()) {
                        val journal = iterator.next()
                        if (journal.id.toString() == targetItem.id) {
                            iterator.remove()
                            break
                        }
                    }

                    val json = com.google.gson.Gson().toJson(existingList)
                    prefs.edit().putString("diary_list_$folderId", json).apply()

                } catch (e: Exception) {
                    Log.e("JaehoonExit", "단건 삭제 프리프 반영 중 예외 발생: ${e.message}")
                }

                val folderIsolatedKey = "${targetItem.date}_$folderId"
                sharedPrefs.edit().remove(folderIsolatedKey).apply()

                masterDiaryList.removeAll { it.id == targetItem.id }

                applyFilterAndSort(currentFilterType)
                setupCalendar()

                Toast.makeText(requireContext(), "일지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        )
        exitSheet.show(parentFragmentManager, "FolderExitSheet2")
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}