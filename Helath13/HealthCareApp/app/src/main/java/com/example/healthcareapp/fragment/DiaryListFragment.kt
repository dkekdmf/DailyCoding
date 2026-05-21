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
import com.example.healthcareapp.ConditionCheckActivity
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

    private lateinit var fabAddWorkout: View
    private lateinit var fabCheckCondition: View
    private lateinit var layoutActionMenuBoard: View

    private lateinit var tvFolderName: TextView
    private var tvDiaryTitle: TextView? = null

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button
    private lateinit var btnFloatingPause: ImageView
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
            val selectedEmojiResId = data.getIntExtra("EMOJI_RES_ID", emojiList[0])
            val resultWorkoutType = data.getStringExtra("WORKOUT_TYPE") ?: "개인운동"
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

            setupDiaryList()
            TimerManager.stopTimer()
            val latestJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
            if (latestJournals.isNotEmpty()) {
                val targetJournal = latestJournals.last()
                val uniqueTimelineKey = "${fullDate}_${folderId}_${targetJournal.startTime}"
                sharedPrefs.edit().putInt(uniqueTimelineKey, selectedEmojiResId).apply()
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
        Log.d("JaehoonIdCheck", "프래그먼트 진입 - 폴더 ID: $folderId, 폴더 이름: $folderName")
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

        fabAddWorkout = view.findViewById(R.id.fab_start_workout)
        fabCheckCondition = view.findViewById(R.id.condition_check)
        layoutActionMenuBoard = view.findViewById(R.id.layout_action_menu_board)

        tvFolderName = view.findViewById(R.id.tv_folder_name)
        tvDiaryTitle = view.findViewById(R.id.tv_diary_title)
        layoutFloatingTimer = view.findViewById(R.id.layout_floating_timer)
        tvFloatingTimer = layoutFloatingTimer.findViewById(R.id.tv_bar_time)
        btnFloatingFinish = layoutFloatingTimer.findViewById(R.id.btn_bar_finish)
        btnFloatingPause = view.findViewById(R.id.btn_bar_pause)
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
            applyFilterByCalendarDate(clickedItem.fullDate)
        }
        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        diaryAdapter = DiaryAdapter(displayList, { item ->
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("SELECT_TAB", 1)
                putExtra("DIARY_DATE", item.date)
                putExtra("EMOJI_RES_ID", item.emojiResId)
                putExtra("DIARY_ID", item.id)
                putExtra("FOLDER_ID", folderId)
            }
            startActivity(intent)
        }, { position ->
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

    private fun applyFilterAndSort(filterType: String) {
        currentFilterType = filterType
        updateFilterUI(filterType)

        var filteredList = masterDiaryList.toList()
        filteredList = when (filterType) {
            "최근순" -> filteredList.sortedByDescending { it.date }
            "오래된순" -> filteredList.sortedBy { it.date }
            "운동한 날만" -> filteredList.sortedByDescending { it.date }
            "사진 있는 일지" -> filteredList.filter { it.imageString.isNotEmpty() }.sortedByDescending { it.date }
            "사진 없는 일지" -> filteredList.filter { it.imageString.isEmpty() }.sortedByDescending { it.date }
            else -> filteredList
        }

        displayList.clear()
        displayList.addAll(filteredList)
        diaryAdapter.notifyDataSetChanged()
    }

    private fun applyFilterByCalendarDate(date: String) {
        val dateFilteredList = masterDiaryList.filter { it.date == date }
        displayList.clear()
        displayList.addAll(dateFilteredList)
        diaryAdapter.notifyDataSetChanged()
    }

    private fun setupDiaryList() {
        masterDiaryList.clear()

        val savedJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
        if (savedJournals.isNotEmpty()) {
            val sortedJournals = savedJournals.sortedByDescending { it.createdAt }

            val savedItems = sortedJournals.map { journal ->
                val dateKey = if (!journal.createdAt.isNullOrEmpty() && journal.createdAt.length >= 10) {
                    journal.createdAt.take(10)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
                }

                val folderIsolatedKey = "${dateKey}_$folderId"

                val myEmoticons = intArrayOf(
                    R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3, R.drawable.emoticon4, R.drawable.emoticon5
                )

                val mixKey = "${dateKey}_$folderId"
                var distributionId = 7
                for (ch in mixKey) {
                    distributionId = distributionId * 31 + ch.code
                }
                val stableIndex = kotlin.math.abs(distributionId) % 5
                val cleanEmojiId = myEmoticons[stableIndex]

                sharedPrefs.edit().putInt(folderIsolatedKey, cleanEmojiId).apply()

                DiaryItem(
                    id = journal.id.toString(),
                    date = dateKey,
                    title = journal.workoutType,
                    emojiResId = cleanEmojiId,
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

        val myEmoticons = intArrayOf(
            R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3, R.drawable.emoticon4, R.drawable.emoticon5
        )

        rawDays.forEach { day ->
            day.isSelected = (day.fullDate == selectedDateStr)

            val folderIsolatedKey = "${day.fullDate}_$folderId"
            val savedEmojiId = sharedPrefs.getInt(folderIsolatedKey, -1)

            if (savedEmojiId != -1) {
                day.hasExercise = true
                day.emojiResId = if (savedEmojiId in 0..4) myEmoticons[savedEmojiId] else savedEmojiId
            } else {
                val logsForDay = masterDiaryList.filter { it.date == day.fullDate }

                if (logsForDay.isNotEmpty()) {
                    val representativeLog = logsForDay.last()
                    day.hasExercise = true

                    val cleanEmojiId = if (representativeLog.emojiResId in 0..4) myEmoticons[representativeLog.emojiResId] else representativeLog.emojiResId
                    day.emojiResId = cleanEmojiId

                    sharedPrefs.edit().putInt(folderIsolatedKey, cleanEmojiId).apply()
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

        btnSortIcon.setOnClickListener {
            val nextFilter = if (currentFilterType == "최근순") {
                "private_images"
            } else {
                "최근순"
            }
            currentFilterType = nextFilter
            applyFilterAndSort(currentFilterType)
        }

        // 🎯 [운동 시작] 버튼 클릭 가드 보강
        fabAddWorkout.setOnClickListener {
            // 구형 폴더 ID가 유실되었거나 정상적이지 않을 때를 대비한 핵심 방어선
            val finalFolderId = if (folderId == -1L) {
                arguments?.getLong("FOLDER_ID", -1L) ?: -1L
            } else {
                folderId
            }

            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                putExtra("IS_SHARED_MODE", isSharedMode)
                // 💡 프리프 매싱 에러를 잡기 위해 Long과 String 둘 다 인텐트에 구워버립니다.
                putExtra("FOLDER_ID", finalFolderId)
                putExtra("FOLDER_ID_STR", finalFolderId.toString())
                putExtra("FOLDER_NAME", folderName ?: "일지 폴더")
            }
            workoutResultLauncher.launch(intent)
        }

        // 🎯 [컨디션 체크] 버튼 클릭 가드 보강
        fabCheckCondition.setOnClickListener {
            val finalFolderId = if (folderId == -1L) {
                arguments?.getLong("FOLDER_ID", -1L) ?: -1L
            } else {
                folderId
            }

            val intent = Intent(requireContext(), ConditionCheckActivity::class.java).apply {
                putExtra("FOLDER_ID", finalFolderId)
                putExtra("FOLDER_ID_STR", finalFolderId.toString()) // 2중 타입 백업
                putExtra("FOLDER_NAME", folderName ?: "일지 폴더")
                putExtra("IS_SHARED_MODE", isSharedMode)
                putExtra("SELECT_TAB", 1)
            }
            startActivity(intent)
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
                layoutActionMenuBoard.visibility = View.GONE

                if (isRunning) {
                    btnFloatingPause.setImageResource(R.drawable.pause)
                } else {
                    btnFloatingPause.setImageResource(R.drawable.play)
                }
            } else {
                layoutFloatingTimer.visibility = View.GONE
                layoutActionMenuBoard.visibility = View.VISIBLE
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
            folderName = "${targetItem.date} 일지",
            onExitConfirm = {
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
            val imageView = ImageView(context).apply {
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