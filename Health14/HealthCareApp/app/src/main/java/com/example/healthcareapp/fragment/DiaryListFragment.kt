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

    // 🎯 [모션 뷰 컴포넌트 변수 교체]
    private lateinit var layoutActionMenuBoard: ViewGroup
    private lateinit var fabStartWorkoutClickZone: FrameLayout
    private lateinit var ivFabPlusIcon: ImageView
    private lateinit var layoutWorkoutText: View
    private lateinit var conditionCheck: View
    private lateinit var menuDivider: View
    private var isMenuExpanded = false // 💡 현재 플러스 버튼이 메뉴판으로 커졌는지 상태 추적용

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

            // 운동 완료 후 돌아오면 콤팩트 모드로 안전 원복
            collapseActionMenu()
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

        // 🎯 [수술 포인트] 새롭게 설계된 XML 모션 아이디들과 매핑 파이프라인 연결
        layoutActionMenuBoard = view.findViewById(R.id.layout_action_menu_board)
        fabStartWorkoutClickZone = view.findViewById(R.id.fab_start_workout_click_zone)
        ivFabPlusIcon = view.findViewById(R.id.iv_fab_plus_icon)
        layoutWorkoutText = view.findViewById(R.id.layout_workout_text)
        conditionCheck = view.findViewById(R.id.condition_check)
        menuDivider = view.findViewById(R.id.menu_divider)

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

        // 🎯 [시안 매칭 확장 모션 인터폴레이션 코어 루틴]
        fabStartWorkoutClickZone.setOnClickListener {
            // 안드로이드 젯팩 트랜지션 매니저 가동 (가로세로 크기 변경 + 투명도 변형 자동 연산)
            androidx.transition.TransitionManager.beginDelayedTransition(
                layoutActionMenuBoard,
                androidx.transition.TransitionSet().apply {
                    addTransition(androidx.transition.ChangeBounds())
                    addTransition(androidx.transition.Fade())
                    duration = 220 // 피그마 규격 220ms 보간
                }
            )

            if (!isMenuExpanded) {
                // 🚀 상태 1: 작은 플러스 버튼을 누르면 전체 메뉴판으로 크기 확장 트랜지션 활성화
                ivFabPlusIcon.visibility = View.GONE // + 마크 자연스럽게 페이드아웃

                // 프레임 레이아웃 크기를 문자열이 전부 펼쳐지도록 매치패런트로 변경
                val params = fabStartWorkoutClickZone.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                fabStartWorkoutClickZone.layoutParams = params

                // 숨겨져 있던 운동 시작 글씨 정보와 위쪽 컨디션 체크 메뉴를 화면에 도킹
                layoutWorkoutText.visibility = View.VISIBLE
                conditionCheck.visibility = View.VISIBLE
                menuDivider.visibility = View.VISIBLE

                isMenuExpanded = true
            } else {
                // 🚀 상태 2: 이미 열려있는 상태에서 '운동 시작' 검은 영역을 누르면 화면 전환 슛!
                val finalFolderId = if (folderId == -1L) {
                    arguments?.getLong("FOLDER_ID", -1L) ?: -1L
                } else {
                    folderId
                }

                val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                    putExtra("IS_SHARED_MODE", isSharedMode)
                    putExtra("FOLDER_ID", finalFolderId)
                    putExtra("FOLDER_ID_STR", finalFolderId.toString())
                    putExtra("FOLDER_NAME", folderName ?: "일지 폴더")
                }
                workoutResultLauncher.launch(intent)
            }
        }

        // 🎯 [컨디션 체크] 확장 상태에서 상단 메뉴를 터치했을 때 작동하는 리스너
        conditionCheck.setOnClickListener {
            if (isMenuExpanded) {
                val finalFolderId = if (folderId == -1L) {
                    arguments?.getLong("FOLDER_ID", -1L) ?: -1L
                } else {
                    folderId
                }

                val intent = Intent(requireContext(), ConditionCheckActivity::class.java).apply {
                    putExtra("FOLDER_ID", finalFolderId)
                    putExtra("FOLDER_ID_STR", finalFolderId.toString())
                    putExtra("FOLDER_NAME", folderName ?: "일지 폴더")
                    putExtra("IS_SHARED_MODE", isSharedMode)
                    putExtra("SELECT_TAB", 1)
                }
                startActivity(intent)

                // 터치 후 콤팩트 모드로 부드럽게 복구
                collapseActionMenu()
            }
        }

        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
    }

    // 🎯 메뉴 보드를 원래의 작고 귀여운 플러스(+) 플로팅 단추로 리턴시키는 역트랜지션 함수
    private fun collapseActionMenu() {
        if (!isMenuExpanded) return

        androidx.transition.TransitionManager.beginDelayedTransition(
            layoutActionMenuBoard,
            androidx.transition.TransitionSet().apply {
                addTransition(androidx.transition.ChangeBounds())
                addTransition(androidx.transition.Fade())
                duration = 200
            }
        )

        ivFabPlusIcon.visibility = View.VISIBLE
        layoutWorkoutText.visibility = View.GONE
        conditionCheck.visibility = View.GONE
        menuDivider.visibility = View.GONE

        val params = fabStartWorkoutClickZone.layoutParams
        val density = resources.displayMetrics.density
        params.width = (44 * density).toInt() // 44dp px 변환 적용
        params.height = (44 * density).toInt()
        fabStartWorkoutClickZone.layoutParams = params

        isMenuExpanded = false
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
        // 🎯 [철벽 가드 1] 옵저버가 켜지기 전에 '실제 돌고 있는 상태'가 아니라면
        // 이전 LiveData 잔상에 의해 뷰가 튀어 오르지 못하도록 강제로 미리 숨겨버립니다.
        if (!TimerManager.isRunning()) {
            layoutFloatingTimer.visibility = View.GONE
            layoutActionMenuBoard.visibility = View.VISIBLE
        }

        TimerManager.timeLiveData.observe(viewLifecycleOwner) { time ->
            val isTimerActive = TimerManager.isTimerActive()
            val isRunning = TimerManager.isRunning()

            // 🎯 [철벽 가드 2] 시간이 아무리 0보다 커도, 현재 '실제 실행 중(isRunning)'인 타이머가 없다면
            // 화면 진입 시 절대로 타이머 바를 노출하지 않습니다.
            if (time > 0 && isTimerActive && isRunning) {
                layoutFloatingTimer.visibility = View.VISIBLE
                tvFloatingTimer.text = TimerManager.getFormattedTime()
                layoutActionMenuBoard.visibility = View.GONE

                if (isMenuExpanded) {
                    isMenuExpanded = false
                    ivFabPlusIcon.visibility = View.VISIBLE
                    layoutWorkoutText.visibility = View.GONE
                    conditionCheck.visibility = View.GONE
                    menuDivider.visibility = View.GONE
                    val params = fabStartWorkoutClickZone.layoutParams
                    val density = resources.displayMetrics.density
                    params.width = (44 * density).toInt()
                    params.height = (44 * density).toInt()
                    fabStartWorkoutClickZone.layoutParams = params
                }

                btnFloatingPause.setImageResource(R.drawable.pause)

            } else if (time > 0 && isTimerActive && !isRunning) {
                // 💡 [유저 시나리오 구제] 사용자가 타이머를 돌리다가 직접 '일시정지'를 누른 게 맞을 때만
                // 타이머 바를 유지하고 play 스킨으로 대기합니다. (진입 잔상으로 멈춘 경우는 아래 else로 빠짐)

                // 🎯 [핵심] 만약 액티비티 진입 초기 단계인데 타이머만 멈춰있는 상태라면 유저가 누른 게 아니므로 숨김!
                if (TimerManager.getFormattedTime() == "00:00:00" || TimerManager.getFormattedTime().isEmpty()) {
                    layoutFloatingTimer.visibility = View.GONE
                    layoutActionMenuBoard.visibility = View.VISIBLE
                } else {
                    layoutFloatingTimer.visibility = View.VISIBLE
                    tvFloatingTimer.text = TimerManager.getFormattedTime()
                    layoutActionMenuBoard.visibility = View.GONE
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