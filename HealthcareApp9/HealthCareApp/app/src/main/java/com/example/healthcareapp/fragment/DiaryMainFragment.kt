package com.example.healthcareapp.fragment

import android.content.Intent
import android.os.Bundle
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
        setupModeUI() // UI 텍스트 설정을 먼저 실행
        setupCalendar() // 저장된 데이터를 불러와 캘린더 구성
        initClickListeners()
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
        // 🎯 [수정] isSharedMode 값에 따라 헤더 텍스트를 확실하게 변경
        tvDiaryHeader?.text = if (isSharedMode) "공유 일지" else "나의 일지"
        tvFolderTitle?.text = folderName ?: "일지"
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        // 🎯 [추가] SharedPreferences에서 해당 폴더의 전체 기록을 가져와 이모티콘 표시 여부 결정
        val allJournals = DiaryPrefsManager.getJournals(requireContext(), folderId)
        val doneDates = allJournals.map { it.createdAt.take(10) }.toSet()

        days.forEach { day ->
            if (doneDates.contains(day.fullDate)) {
                day.hasExercise = true
                // 프로젝트에 등록된 이모티콘 리소스 ID를 넣어주세요 (예: R.drawable.ic_check)
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
                // 🎯 기존 HomeActivity의 이동 로직 유지
                (activity as? HomeActivity)?.moveToDiaryList(folderId, folderName, isSharedMode)
            }
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar() // 주간 이동 시 다시 데이터를 로드하여 이모티콘 갱신
    }

    private fun updateDiaryList(date: String) {
        // 선택 날짜에 따른 추가 로직이 필요하다면 작성
        Log.d("JaehoonLog", "선택된 날짜: $date")
    }
}