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
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.WorkoutExerciseActivity
import com.example.healthcareapp.WorkoutFinishActivity
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DayItem
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.data.JournalApiResponse
import com.example.healthcareapp.data.JournalSummaryDto
import com.example.healthcareapp.network.RetrofitClient
import com.example.healthcareapp.sheet.FolderExitSheet2
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
    private lateinit var fabStartWorkout: ImageButton
    private lateinit var tvFolderName: TextView
    private var tvDiaryTitle: TextView? = null

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button
    private lateinit var btnFloatingPause: ImageView

    private var currentFilterType = "최근순"
    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter

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
            Log.d("JaehoonLog", "운동 완료 신호 수신 - 서버 새로고침 및 타이머 파괴")

            TimerManager.stopAndResetTimer()

            layoutFloatingTimer.visibility = View.GONE
            fabStartWorkout.visibility = View.VISIBLE

            loadDiariesFromServer()
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
        loadDiariesFromServer()
    }

    private fun loadDiariesFromServer() {
        RetrofitClient.journalService.getMyJournals(null, null, null)
            .enqueue(object : retrofit2.Callback<JournalApiResponse<List<JournalSummaryDto>>> {
                override fun onResponse(
                    call: retrofit2.Call<JournalApiResponse<List<JournalSummaryDto>>>,
                    response: retrofit2.Response<JournalApiResponse<List<JournalSummaryDto>>>
                ) {
                    if (response.isSuccessful) {
                        Log.d("JaehoonLog", "서버 원본 응답 바디: ${response.body()?.toString()}")
                        val serverList = response.body()?.data ?: emptyList()

                        masterDiaryList.clear()

                        serverList.forEach { dto ->
                            Log.d("JaehoonLog", "서버에서 온 DTO 상세: $dto")

                            val diaryIdLong = dto.id
                            val emojiIndex = (diaryIdLong % emojiList.size).toInt()
                            val selectedEmoji = emojiList[emojiIndex]

                            val diaryItem = DiaryItem(
                                id = dto.id.toString(),
                                date = dto.workoutDate,
                                title = dto.workoutType ?: "개인운동",
                                emojiResId = selectedEmoji
                            )
                            masterDiaryList.add(diaryItem)
                        }

                        isWorkoutCompletedToday = checkTodayWorkout(masterDiaryList)
                        applyFilterAndSort(currentFilterType)
                        setupCalendar()

                        Log.d("JaehoonLog", "서버 동기화 완료: ${masterDiaryList.size}건")
                    } else {
                        Log.e("JaehoonLog", "목록 조회 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<JournalApiResponse<List<JournalSummaryDto>>>, t: Throwable) {
                    Log.e("JaehoonLog", "네트워크 에러: ${t.message}")
                }
            })
    }

    private fun checkTodayWorkout(list: List<DiaryItem>): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        return list.any { it.date == today }
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
        btnFloatingPause = layoutFloatingTimer.findViewById(R.id.btn_bar_pause)
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

        diaryAdapter = DiaryAdapter(displayList, { item ->
            Log.d("JaehoonTest", "상세화면 이동 - 보낼 색상 ID: ${item.emojiResId}")
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("SELECT_TAB", 1)
                putExtra("DIARY_DATE", item.date)
                putExtra("EMOJI_RES_ID", item.emojiResId)
                putExtra("DIARY_ID", item.id)
            }
            startActivity(intent)
        }, { position ->
            val targetItem = displayList[position]
            val diaryId = targetItem.id
            val currentFolder = folderName ?: "일지"

            val exitSheet = FolderExitSheet2(currentFolder) {
                Log.d("JaehoonLog", "바텀시트에서 나가기 확정 -> soft-delete 요청 시도! 일지 ID: $diaryId")

                RetrofitClient.journalService.deleteJournal(diaryId.toLong())
                    .enqueue(object : retrofit2.Callback<JournalApiResponse<Void>> {
                        override fun onResponse(
                            call: retrofit2.Call<JournalApiResponse<Void>>,
                            response: retrofit2.Response<JournalApiResponse<Void>>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("JaehoonLog", "🎉 스프링 DB에서 일지 soft-delete 반영 성공!")

                                masterDiaryList.removeAll { it.id == diaryId }
                                displayList.removeAt(position)

                                diaryAdapter.notifyItemRemoved(position)
                                diaryAdapter.notifyItemRangeChanged(position, displayList.size)

                                isWorkoutCompletedToday = checkTodayWorkout(masterDiaryList)
                                setupCalendar()
                            } else {
                                Log.e("JaehoonLog", "삭제 실패 코드: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<JournalApiResponse<Void>>, t: Throwable) {
                            Log.e("JaehoonLog", "삭제 네트워크 통신 실패: ${t.message}")
                        }
                    })
            }
            exitSheet.show(parentFragmentManager, "FolderExitSheet2")
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
        applyFilterAndSort("최근순")
    }

    private fun setupCalendar() {
        val (title, rawDays) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        rawDays.forEach { day ->
            day.isSelected = (day.fullDate == selectedDateStr)

            if (day.fullDate == todayStr && isWorkoutCompletedToday) {
                val todayLog = masterDiaryList.find { it.date == todayStr }
                if (todayLog != null) {
                    day.hasExercise = true
                    day.emojiResId = todayLog.emojiResId
                }
            } else if (day.fullDate == todayStr && !isWorkoutCompletedToday) {
                day.hasExercise = false
            }
        }

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

        // 고정형 플러스 버튼 클릭 시 곧바로 운동 시작 액티비티 런처 호출
        fabStartWorkout.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                putExtra("IS_SHARED_MODE", isSharedMode)
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
        TimerManager.timeLiveData.observe(viewLifecycleOwner) { time ->
            val formattedTime = TimerManager.getFormattedTime()

            if (!TimerManager.isTimerActive() || formattedTime == "00:00" || formattedTime == "00:00:00" || time == 0) {
                layoutFloatingTimer.visibility = View.GONE
                fabStartWorkout.visibility = View.VISIBLE
            } else {
                layoutFloatingTimer.visibility = View.VISIBLE
                tvFloatingTimer.text = formattedTime
                fabStartWorkout.visibility = View.GONE
            }
        }

        btnFloatingFinish.setOnClickListener { showFinishDialog() }
    }

    private fun showFinishDialog() {
        if (TimerManager.isRunning()) {
            TimerManager.pauseTimer()
            btnFloatingPause.setImageResource(R.drawable.play)
        }

        val totalTime = TimerManager.getFormattedTime()

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val now = Date()
        val endTime = sdf.format(now)

        val realStartTime = try {
            val totalSeconds = timeToSeconds(totalTime)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA).apply {
                time = now
                add(Calendar.SECOND, -totalSeconds)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            TimerManager.getWorkoutStartTime()
        }

        Log.d("JaehoonLog", "🎯 [인텐트 전송 직전 교정 검증] 시작시간: $realStartTime, 종료시간: $endTime, total시간: $totalTime")

        val dialog = WorkoutFinishDialog {
            val intent = Intent(requireContext(), WorkoutFinishActivity::class.java).apply {
                putExtra("TOTAL_TIME", totalTime)
                putExtra("START_TIME", realStartTime)
                putExtra("END_TIME", endTime)
                putExtra("IS_SHARED_MODE", isSharedMode)
                putExtra("FOLDER_ID", arguments?.getLong("FOLDER_ID", 163L) ?: 163L)
            }
            workoutResultLauncher.launch(intent)
        }
        dialog.show(parentFragmentManager, "WorkoutFinishDialog")
    }

    private fun timeToSeconds(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            val seconds = parts[2].toInt()
            (hours * 3600) + (minutes * 60) + seconds
        } catch (e: Exception) {
            0
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}