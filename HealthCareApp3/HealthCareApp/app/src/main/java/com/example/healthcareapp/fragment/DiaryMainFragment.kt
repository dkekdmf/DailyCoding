package com.example.healthcareapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.ConditionCheckActivity

import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
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
        // onCreateView에서는 레이아웃만 생성하여 반환합니다.
        return inflater.inflate(R.layout.calendarbar, container, false)
    }

    // ⭐ 핵심 수정: 뷰가 완전히 생성된 직후인 이 시점에 초기화 로직을 실행해야 버튼이 제대로 동작합니다.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCalendar()
        setupModeUI()
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
        tvDiaryHeader?.text = if (isSharedMode) "공유 일지" else "나의 일지"
        tvFolderTitle?.text = folderName ?: "일지"
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

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

            // 1. 왼쪽 상단 화살표 (폴더 리스트로 돌아가기)
            safeView.findViewById<ImageView>(R.id.arrow_btn)?.setOnClickListener {
                // 스택에 이전 프래그먼트가 있다면 뒤로 가고, 없다면 액티비티를 종료하거나 홈으로 이동합니다.
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    // 스택이 비어있을 경우 액티비티 자체의 뒤로가기 기능을 실행 (폴더 리스트로 복귀)
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }

            // ... 나머지 버튼 리스너들 (주간 이동, 컨디션 체크 등) ...
            btnPrevWeek.setOnClickListener { moveWeek(-1) }
            btnNextWeek.setOnClickListener { moveWeek(1) }

//            conditionCheckBtn.setOnClickListener {
//                val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
//                    putExtra("FOLDER_ID", folderId)
//                    putExtra("FOLDER_NAME", folderName)
//                    putExtra("IS_SHARED_MODE", isSharedMode)
//                    putExtra("SELECT_TAB", 1)
//                }
//                startActivity(intent)
//            }
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
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title
        dayAdapter.updateData(days)
    }

    private fun updateDiaryList(date: String) {
        // 선택된 날짜에 따른 업데이트 로직
    }
}