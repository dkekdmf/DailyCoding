package com.example.healthcareapp.fragment

import WorkoutFinishDialog
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
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

    // 🎯 [신규 추가] 알려주신 필터링 버튼 변수 선언
    private lateinit var btnSortIcon: ImageView

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
            Log.d("JaehoonLog", "운동 완료 후 복귀: 리스트 갱신 시작")
            val selectedEmojiResId = data.getIntExtra("EMOJI_RES_ID", emojiList[0])
            val resultWorkoutType = data.getStringExtra("WORKOUT_TYPE") ?: "개인운동"
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

            Log.d("JaehoonSync", "🎬 운동 완료 리스트 복귀 수신 완료!")

            setupDiaryList()
            TimerManager.stopTimer()
            val latestJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
            if (latestJournals.isNotEmpty()) {
                val targetJournal = latestJournals.last()
                val uniqueTimelineKey = "${fullDate}_${folderId}_${targetJournal.startTime}"

                sharedPrefs.edit().putInt(uniqueTimelineKey, selectedEmojiResId).apply()
                Log.d("JaehoonSync", "🔑 격리된 고유 캘린더 Key 생성 성공 -> $uniqueTimelineKey")
            }

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

        // 🎯 알려주신 btn_sort_icon 컴포넌트 바인딩 완료
        btnSortIcon = view.findViewById(R.id.btn_sort_icon)
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
        }, { clickedItem ->
            if (!clickedItem.imageString.isNullOrEmpty()) {
                showImagePopup(clickedItem.imageString, clickedItem.date)
            }
        })

        rvDiaryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diaryAdapter
        }
    }

    // 🎯 [정밀 수술 완료] 사진 필터링 조건을 포함하여 리스트를 실시간으로 스위칭합니다.
    private fun applyFilterAndSort(filterType: String) {
        currentFilterType = filterType
        updateFilterUI(filterType)

        var filteredList = masterDiaryList.toList()
        filteredList = when (filterType) {
            "최근순" -> filteredList.sortedByDescending { it.date }
            "오래된순" -> filteredList.sortedBy { it.date }
            "운동한 날만" -> filteredList.sortedByDescending { it.date }

            // 📸 사진이 한 장이라도 있는 일지만 조준 필터링
            "사진 있는 일지" -> filteredList.filter { it.imageString.isNotEmpty() }.sortedByDescending { it.date }

            // 🚫 사진이 등록되지 않은 일지만 조준 필터링
            "사진 없는 일지" -> filteredList.filter { it.imageString.isEmpty() }.sortedByDescending { it.date }

            else -> filteredList
        }

        displayList.clear()
        displayList.addAll(filteredList)
        diaryAdapter.notifyDataSetChanged()
    }

    private fun setupDiaryList() {
        masterDiaryList.clear()

        val savedJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
        Log.d("JaehoonLog", "로드된 일지 개수: ${savedJournals.size} (폴더 ID: $folderId)")
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
                    title = journal.workoutType,
                    emojiResId = journal.emojiResId,
                    imageString = journal.imageString ?: ""
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
                val logsForDay = masterDiaryList.filter { it.date == day.fullDate }

                if (logsForDay.isNotEmpty()) {
                    val representativeLog = logsForDay.last()

                    day.hasExercise = true
                    day.emojiResId = representativeLog.emojiResId

                    sharedPrefs.edit().putInt(folderIsolatedKey, representativeLog.emojiResId).apply()
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

        // ==================================================================
        // 🎯 [필터 단추 부킹 완공] btnSortIcon을 누를 때마다 필터 조건이 3단계로 무한 순환합니다.
        // 최근순(전체) ➔ 사진 있는 일지 ➔ 사진 없는 일지 ➔ 다시 최근순
        // ==================================================================
        btnSortIcon.setOnClickListener {
            val nextFilter = if (currentFilterType == "최근순") {
                // 1. 현재 전체보기 상태라면 ➔ 사진 있는 일지만 보기로 전환
                "사진 있는 일지"
            } else {
                // 2. 현재 사진 필터링 상태라면 ➔ 다시 원래 전체 리스트(최근순)로 복구
                "최근순"
            }

            // 전역 변수에 최신 필터 상태 즉시 저장
            currentFilterType = nextFilter

            // 어떤 모드로 바뀌었는지 유저에게 토스트 피드백 제공
            val toastMessage = if (currentFilterType == "사진 있는 일지") "필터: 사진 있는 일지" else "필터: 전체 보기"
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()

            // 화면 리사이클러뷰에 즉시 반영 슛!
            applyFilterAndSort(currentFilterType)
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

    private fun showImagePopup(base64String: String, dateText: String) {
        try {
            val decodedString: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            val imageView = ImageView(requireContext()).apply {
                setImageBitmap(decodedByte)
                adjustViewBounds = true
                setPadding(40, 40, 40, 40)
            }

            builder.setTitle("$dateText 인증샷")
                .setView(imageView)
                .setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            Log.e("JaehoonLog", "팝업 이미지 디코딩 실패: ${e.message}")
        }
    }
}