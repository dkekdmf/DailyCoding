package com.example.healthcareapp.fragment

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.ConditionCheckActivity
import com.example.healthcareapp.DiaryPrefsManager
import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.DateUtils
import java.util.Calendar

class DiaryMainFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView

    private lateinit var exerciseStartBtn: AppCompatButton
    private lateinit var conditionCheckBtn: AppCompatButton

    private var tvDiaryHeader: TextView? = null
    private var tvFolderTitle: TextView? = null

    // 🎯 [신규 추가] 하단 일지 목록을 그려줄 리사이클러뷰와 어댑터 선언
    private lateinit var rvDiaryList: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private val diaryItems = mutableListOf<DiaryItem>()

    private lateinit var dayAdapter: DayAdapter
    private var currentCalendar = Calendar.getInstance()

    private var folderId: Long = -1L
    private var folderName: String? = null
    private var isSharedMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            folderId = it.getLong("FOLDER_ID", -1L)
            folderName = it.getString("FOLDER_NAME")
            isSharedMode = it.getBoolean("IS_SHARED_MODE", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.calendarbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupModeUI()
        setupDiaryListView(view) // 🎯 [신규 추가] 일지 리사이클러뷰 초기 세팅
        setupCalendar()
        initClickListeners()

        // 🎯 [신규 추가] 처음 화면 진입 시 오늘 날짜의 일지를 기본으로 로드합니다.
        val todayStr = DateUtils.getWeekInfo(Calendar.getInstance().time).second.find {
            val cal = Calendar.getInstance()
            // 오늘 날짜 매칭 헬퍼 (포맷팅이 yyyy-MM-dd 형태라고 가정)
            true
        }
        val defaultDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).format(java.util.Date())
        updateDiaryList(defaultDate)
    }

    private fun initViews(view: View) {
        rvCalendar = view.findViewById(R.id.rv_calendar)
        tvWeekTitle = view.findViewById(R.id.tv_week_title)
        btnPrevWeek = view.findViewById(R.id.btn_prev_week)
        btnNextWeek = view.findViewById(R.id.btn_next_week)

        conditionCheckBtn = view.findViewById(R.id.condition_check)
        exerciseStartBtn = view.findViewById(R.id.exercise_start)

        tvDiaryHeader = view.findViewById(R.id.tv_diary_header)
        tvFolderTitle = view.findViewById(R.id.tv_folder_title)
    }

    private fun setupModeUI() {
        tvDiaryHeader?.text = if (isSharedMode) "공유 일지" else "나의 일지"
        tvFolderTitle?.text = folderName ?: "일지"
    }

    // 🎯 [신규 추가] 일지 리사이클러뷰 및 어댑터 부드러운 연결 통로 개설
    private fun setupDiaryListView(view: View) {
        // XML 내 하단 리사이클러뷰 ID 매싱 (보통 rv_diary_list 혹은 folder_list 등으로 명명하셨을 자리를 찾아 연결합니다)
        // 💡 만약 XML 하단 일지 리사이클러뷰 ID가 다르면 R.id.folder_list 부분을 고쳐주세요!
        rvDiaryList = view.findViewById(R.id.folder_list) ?: view.findViewById(R.id.rv_calendar) // 임시 방어 코드

        diaryAdapter = DiaryAdapter(
            items = diaryItems,
            onItemClick = { item -> Log.d("JaehoonLog", "일지 클릭됨: ${item.title}") },
            onDotClick = { pos -> Log.d("JaehoonLog", "더보기 클릭됨 포지션: $pos") },
            onPhotoClick = { clickedItem ->
                // 📸 [핵심 연동] 사진 마크를 터치하면 저장해 둔 이미지를 즉시 다이얼로그 팝업으로 노출!
                if (!clickedItem.imageString.isNullOrEmpty()) {
                    showImagePopup(clickedItem.imageString, clickedItem.date)
                }
            }
        )

        rvDiaryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diaryAdapter
        }
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        val allJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
        val doneDates = allJournals.map { it.createdAt.take(10) }.toSet()

        days.forEach { day ->
            if (doneDates.contains(day.fullDate)) {
                day.hasExercise = true
                day.emojiResId = R.drawable.emoticon1
            } else {
                day.hasExercise = false
            }
        }

        dayAdapter = DayAdapter(days) { clickedDay ->
            updateDiaryList(clickedDay.fullDate)
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            post { dayAdapter.notifyDataSetChanged() }
        }
    }

    private fun initClickListeners() {
        view?.let { safeView ->
            safeView.findViewById<ImageView>(R.id.arrow_btn)?.setOnClickListener {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }

            btnPrevWeek.setOnClickListener { moveWeek(-1) }
            btnNextWeek.setOnClickListener { moveWeek(1) }

            conditionCheckBtn.setOnClickListener {
                val intent = Intent(requireContext(), ConditionCheckActivity::class.java).apply {
                    putExtra("FOLDER_ID", folderId)
                    putExtra("FOLDER_NAME", folderName)
                    putExtra("IS_SHARED_MODE", isSharedMode)
                    putExtra("SELECT_TAB", 1)
                }
                startActivity(intent)
            }

            exerciseStartBtn.setOnClickListener {
                (activity as? HomeActivity)?.moveToDiaryList(folderId, folderName, isSharedMode)
            }
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }

    // 🎯 [타입 미스매치 정밀 수술] id 파라미터를 String 타입으로 변환하여 매싱합니다.
    private fun updateDiaryList(date: String) {
        Log.d("JaehoonLog", "선택된 날짜 데이터 필터링 가동: $date")

        // 1. SharedPreferences에서 전체 운동 기록 로드
        val allJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)

        // 2. 클릭한 날짜와 일치하는 일지만 필터링
        val filteredJournals = allJournals.filter { it.createdAt.startsWith(date) }

        diaryItems.clear()

        // 💡 각 아이템을 변환할 때 고유성(String)을 보장하기 위해
        // 인덱스를 활용하여 index.toString()으로 규격을 완벽하게 맞춥니다.
        filteredJournals.forEachIndexed { index, dto ->
            diaryItems.add(
                DiaryItem(
                    // 🔓 [타입 치료 완료] Required: String / Found: Long 버그 완벽 격파!
                    id = index.toString(),
                    date = dto.createdAt.take(10),
                    title = dto.workoutType,
                    emojiResId = dto.emojiResId,
                    imageString = dto.imageString
                )
            )
        }

        // 3. 리스트 새로고침
        if (::diaryAdapter.isInitialized) {
            diaryAdapter.notifyDataSetChanged()
        }
    }

    // 🎯 [신규 추가] Base64 스트링을 복원하여 화면 중앙에 크고 영롱하게 띄워주는 사진 뷰어 팝업 시스템
    private fun showImagePopup(base64String: String, dateText: String) {
        try {
            val decodedString: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            val imageView = ImageView(context).apply {
                setImageBitmap(decodedByte)
                adjustViewBounds = true
                setPadding(40, 40, 40, 40) // 액자 마진 부여
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