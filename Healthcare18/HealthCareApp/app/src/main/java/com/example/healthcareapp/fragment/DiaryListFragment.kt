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
import com.example.healthcareapp.*
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DayItem
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.ColorUtils
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

    private lateinit var layoutActionMenuBoard: ViewGroup
    private lateinit var fabStartWorkoutClickZone: FrameLayout
    private lateinit var ivFabPlusIcon: ImageView
    private lateinit var layoutWorkoutText: View
    private lateinit var conditionCheck: View
    private lateinit var menuDivider: View
    private var isMenuExpanded = false

    private lateinit var tvFolderName: TextView
    private var tvDiaryTitle: TextView? = null

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button
    private lateinit var btnFloatingPause: ImageView
    private lateinit var btnSortIcon: ImageView
    private var isWorkoutOnlyFilter = false
    private var currentFilterType = "최근순"
    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter
    private var folderId: Long = -1L
    private var currentCalendar = Calendar.getInstance()
    private var currentDaysList = mutableListOf<DayItem>()
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    private val masterDiaryList = mutableListOf<DiaryItem>()
    private val displayList = ArrayList<DiaryItem>()

    private var folderName: String? = null
    private var isSharedMode = false

    private val workoutResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setupDiaryList()
            TimerManager.stopTimer()
            applyFilterAndSort(currentFilterType)
            setupCalendar()
            dayAdapter.notifyDataSetChanged()
            rvDiaryList.scrollToPosition(0)
            collapseActionMenu()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.folder_detial, container, false)
    }
    override fun onResume() {
        super.onResume()
        // 🚀 화면이 다시 보여질 때마다 무조건 리스트를 최신 상태로 갱신합니다.
        setupDiaryList()
        Log.d("JaehoonSync", "onResume: 리스트 강제 갱신 완료")
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
        dayAdapter = DayAdapter(emptyList(), folderId) { clickedItem ->
            selectedDateStr = clickedItem.fullDate
            setupCalendar()
            applyFilterByCalendarDate(clickedItem.fullDate)
        }
        rvCalendar.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCalendar.adapter = dayAdapter

        diaryAdapter = DiaryAdapter(displayList, folderId, { item ->
            startActivity(Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("DIARY_DATE", item.date); putExtra("DIARY_ID", item.id); putExtra("FOLDER_ID", folderId)
            })
        }, { pos -> showFolderExitSheet(pos) }, { item -> if(!item.imageString.isNullOrEmpty()) showImagePopup(item.imageString, item.date) })
        rvDiaryList.layoutManager = LinearLayoutManager(requireContext())
        rvDiaryList.adapter = diaryAdapter
    }

    private fun applyFilterAndSort(filterType: String) {
        currentFilterType = filterType
        updateFilterUI(filterType)

        val filteredList = when (filterType) {
            "최근순" -> masterDiaryList.sortedByDescending { it.date }
            "오래된순" -> masterDiaryList.sortedBy { it.date }
            // 🎯 운동한 날만 필터: 타이틀이 비어있지 않은 항목(운동 내용이 있는 경우)만 필터링
            "운동한 날만" -> masterDiaryList.filter { it.title.isNotEmpty() }.sortedByDescending { it.date }
            "사진 있는 일지" -> masterDiaryList.filter { it.imageString.isNotEmpty() }.sortedByDescending { it.date }
            "사진 없는 일지" -> masterDiaryList.filter { it.imageString.isEmpty() }.sortedByDescending { it.date }
            else -> masterDiaryList
        }

        displayList.clear()
        displayList.addAll(filteredList)
        diaryAdapter.notifyDataSetChanged()
    }
    private fun applyFilterByCalendarDate(date: String) {
        displayList.clear()
        displayList.addAll(masterDiaryList.filter { it.date == date })
        diaryAdapter.notifyDataSetChanged()
    }
    private fun setupDiaryList() {
        masterDiaryList.clear()
        val savedJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)

        if (savedJournals.isNotEmpty()) {
            // 최근 순으로 정렬하여 리스트 생성
            val sortedJournals = savedJournals.sortedByDescending { it.createdAt }

            val savedItems = sortedJournals.map { journal ->
                val dateKey = if (!journal.createdAt.isNullOrEmpty()) journal.createdAt.take(10) else SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

                DiaryItem(
                    id = journal.id.toString(),
                    date = dateKey,
                    title = journal.workoutType,
                    emojiResId = ColorUtils.getStableEmojiResId(dateKey, journal.id.toString(), folderId),
                    imageString = journal.imageString ?: "",
                    folderId = folderId
                )
            }
            masterDiaryList.addAll(savedItems)
        }
        // 리스트 먼저 정렬하고 갱신
        applyFilterAndSort(currentFilterType)
        // 그 다음 캘린더를 갱신
        setupCalendar()
    }
    private fun setupCalendar() {
        val (title, rawDays) = DateUtils.getWeekInfo(currentCalendar.time, folderId)
        tvWeekTitle.text = title

        rawDays.forEach { day ->
            day.isSelected = (day.fullDate == selectedDateStr)

            // masterDiaryList에서 해당 날짜인 데이터를 찾음 (이미 최신순 정렬됨)
            val dailyJournals = masterDiaryList.filter { it.date == day.fullDate }

            if (dailyJournals.isNotEmpty()) {
                day.hasExercise = true
                // 🎯 무조건 가장 최신(첫 번째) 일지의 이모티콘 ID를 가져옴
                day.emojiResId = dailyJournals.first().emojiResId
            } else {
                day.hasExercise = false
            }
        }
        dayAdapter.updateData(rawDays.toMutableList())
    }


    // 🎯 initClickListeners 수정
    private fun initClickListeners(view: View) {
        view.findViewById<ImageView>(R.id.arrow_btn).setOnClickListener { parentFragmentManager.popBackStack() }
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // 정렬 텍스트 리스너
        view.findViewById<TextView>(R.id.tv_sort_latest)?.setOnClickListener { applyFilterAndSort("최근순") }
        view.findViewById<TextView>(R.id.tv_sort_oldest)?.setOnClickListener { applyFilterAndSort("오래된순") }

        // 🎯 [체크박스 필터] 아이콘 전환 및 필터 적용
        view.findViewById<View>(R.id.cb_only_workout)?.setOnClickListener {
            val nextFilter = if (currentFilterType == "운동한 날만") "최근순" else "운동한 날만"
            applyFilterAndSort(nextFilter)
        }

        // 기존 팝업 정렬 메뉴
        btnSortIcon?.setOnClickListener { view ->
            showPhotoFilterPopup(view)
        }
        // 플로팅 메뉴 버튼 애니메이션 로직
        fabStartWorkoutClickZone.setOnClickListener {
            androidx.transition.TransitionManager.beginDelayedTransition(
                layoutActionMenuBoard as ViewGroup,
                androidx.transition.TransitionSet().apply {
                    addTransition(androidx.transition.ChangeBounds())
                    addTransition(androidx.transition.Fade())
                    duration = 220
                }
            )

            if (!isMenuExpanded) {
                ivFabPlusIcon.visibility = View.GONE
                val params = fabStartWorkoutClickZone.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                fabStartWorkoutClickZone.layoutParams = params

                layoutWorkoutText.visibility = View.VISIBLE
                conditionCheck.visibility = View.VISIBLE
                menuDivider.visibility = View.VISIBLE

                isMenuExpanded = true
            } else {
                val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                    putExtra("FOLDER_ID", folderId)
                    putExtra("FOLDER_NAME", folderName ?: "일지")
                }
                workoutResultLauncher.launch(intent)
                collapseActionMenu()
            }
        }

        // 컨디션 체크 클릭
        conditionCheck.setOnClickListener {
            val intent = Intent(requireContext(), ConditionCheckActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
            }
            startActivity(intent)
            collapseActionMenu()
        }
        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                // 🎯 굳히기: intent.putExtra로 명확하게 넣어주기
                putExtra("FOLDER_ID", this@DiaryListFragment.folderId)
                putExtra("FOLDER_NAME", this@DiaryListFragment.folderName)
            }
            startActivity(intent)
        }
        btnFloatingPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                btnFloatingPause.setImageResource(R.drawable.play) // 일시정지 중이면 재생 아이콘으로
            } else {
                TimerManager.startTimer()
                btnFloatingPause.setImageResource(R.drawable.pause) // 동작 중이면 일시정지 아이콘으로
            }
            TimerManager.timeLiveData.value = TimerManager.timeLiveData.value
        }

        // 🎯 종료 버튼 (피니시 다이얼로그 호출)
        btnFloatingFinish.setOnClickListener {
            showFinishDialog()
        }



        // 운동 시작 텍스트 영역 클릭
        layoutWorkoutText.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
                putExtra("FOLDER_NAME", folderName ?: "일지")
            }
            workoutResultLauncher.launch(intent)
            collapseActionMenu()
        }
    }
    private fun showFinishDialog() {
        val totalTime = TimerManager.getFormattedTime()
        val endTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())
        // 주의: startTime은 TimerManager에 저장된 시작 시간을 가져오는 게 정확합니다.
        val startTime = "00:00:00"

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
    private fun collapseActionMenu() {
        if (!isMenuExpanded) return

        androidx.transition.TransitionManager.beginDelayedTransition(
            layoutActionMenuBoard as ViewGroup,
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

        // 🎯 원래의 작은 사이즈로 복구 (44dp 혹은 기존 사이즈)
        val params = fabStartWorkoutClickZone.layoutParams
        val density = resources.displayMetrics.density
        params.width = (54 * density).toInt() // XML에서 지정한 54dp
        params.height = (54 * density).toInt()
        fabStartWorkoutClickZone.layoutParams = params

        isMenuExpanded = false
    }

    private fun updateFilterUI(filterType: String) {
        val v = view ?: return
        val tvLatest = v.findViewById<TextView>(R.id.tv_sort_latest)
        val tvOldest = v.findViewById<TextView>(R.id.tv_sort_oldest)
        val ivCheck = v.findViewById<ImageView>(R.id.cb_only_workout)

        val colorActive = Color.parseColor("#2E3D4F")
        val colorInactive = Color.parseColor("#B8BDC3")

        // 정렬 텍스트 색상 변경
        tvLatest?.setTextColor(if (filterType == "최근순") colorActive else colorInactive)
        tvOldest?.setTextColor(if (filterType == "오래된순") colorActive else colorInactive)

        // 체크박스 아이콘 상태 동기화
        ivCheck?.setImageResource(if (filterType == "운동한 날만") R.drawable.checkboxblue3x else R.drawable.checkbox3x)
        isWorkoutOnlyFilter = (filterType == "운동한 날만")
    }
    private fun setupFloatingTimerObserver() {
        // 🎯 1. 화면 진입 시점에 타이머 상태에 따른 초기 가시성 강제 설정
        if (TimerManager.isRunning()) {
            layoutFloatingTimer.visibility = View.VISIBLE
            layoutActionMenuBoard.visibility = View.GONE
        } else {
            layoutFloatingTimer.visibility = View.GONE
            layoutActionMenuBoard.visibility = View.VISIBLE
        }

        // 2. 이후 상태 변화 관찰
        TimerManager.timeLiveData.observe(viewLifecycleOwner) { time ->
            val isTimerActive = TimerManager.isTimerActive()
            val isRunning = TimerManager.isRunning()

            if (isTimerActive && (time > 0 || isRunning)) {
                // 운동 중 (일시정지 포함)
                layoutFloatingTimer.visibility = View.VISIBLE
                layoutActionMenuBoard.visibility = View.GONE
                tvFloatingTimer.text = TimerManager.getFormattedTime()
                btnFloatingPause.setImageResource(if (isRunning) R.drawable.puase3x11 else R.drawable.play3x11)
            } else {
                // 운동 종료 및 대기 상태
                layoutFloatingTimer.visibility = View.GONE
                layoutActionMenuBoard.visibility = View.VISIBLE
            }
        }
    }
    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
    private fun filterDiary(hasPhoto: Boolean?) {
        // 1. 선택된 상태를 currentFilterType으로 설정 (팝업용 상태 관리)
        val filterText = when (hasPhoto) {
            true -> "사진 있는 일지"
            false -> "사진 없는 일지"
            else -> "최근순" // 전체 보기 시
        }

        // 2. 기존 정렬 함수를 활용하여 필터링
        applyFilterAndSort(filterText)
    }
    private fun showPhotoFilterPopup(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.dialog_photo_filter, null)

        // 💡 1. 220dp를 픽셀 단위로 변환 (찌그러짐 방지 핵심)
        val widthInPx = (220 * resources.displayMetrics.density).toInt()

        // 💡 2. 너비를 EXACTLY(고정)로 강제 측정
        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(widthInPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val popupWindow = PopupWindow(
            popupView,
            popupView.measuredWidth,  // 이제 고정된 220dp가 적용됨
            popupView.measuredHeight,
            true
        )

        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true

        // 5. 버튼 클릭 리스너 설정
        popupView.findViewById<View>(R.id.btn_filter_has_photo).setOnClickListener {
            filterDiary(hasPhoto = true)
            popupWindow.dismiss()
        }

        popupView.findViewById<View>(R.id.btn_filter_no_photo).setOnClickListener {
            filterDiary(hasPhoto = false)
            popupWindow.dismiss()
        }

        // 6. 위치 지정 (anchorView 아래에 띄움)
        popupWindow.showAsDropDown(anchorView)
    }
    // 🎯 [클래스 내부로 이동 완료]
    private fun showImagePopup(base64String: String, dateText: String) {
        if (!isAdded || context == null) return
        try {
            val decodedString: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            val imageView = ImageView(requireContext()).apply {
                setImageBitmap(decodedByte)
                adjustViewBounds = true
                setPadding(40, 40, 40, 40)
            }

            // 1. builder를 통해 dialog를 먼저 생성합니다.
            val dialog = builder.setTitle("$dateText")
                .setView(imageView)
                .setPositiveButton("닫기") { d, _ -> d.dismiss() }
                .create()

            // 2. 다이얼로그가 화면에 보인 후 버튼 색상을 변경합니다.
            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                positiveButton.setTextColor(Color.BLACK) // 🎯 여기서 검정색으로 변경
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("JaehoonLog", "팝업 이미지 디코딩 실패: ${e.message}")
        }
    }
    private fun showFolderExitSheet(position: Int) {
        val targetItem = displayList.getOrNull(position) ?: return

        // 🎯 필요에 따라 FolderExitSheet2가 import 되어 있는지 확인하세요
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

                // 삭제 후 리스트 및 캘린더 새로고침
                masterDiaryList.removeAll { it.id == targetItem.id }
                applyFilterAndSort(currentFilterType)
                setupCalendar()

                Toast.makeText(requireContext(), "일지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        )
        exitSheet.show(parentFragmentManager, "FolderExitSheet2")
    }
}