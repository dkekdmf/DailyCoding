package com.example.healthcareapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.DiaryPrefsManager
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.adapter.ConditionAdapter
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.FragmentConditionCheckBinding
import com.google.type.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * 사용자의 운동 후 컨디션(통증, 강도, 목표 달성 등) 기록 리스트를 보여주는 프래그먼트
 */
class ConditionCheckFragment : Fragment() {

    // ViewBinding 설정: 메모리 누수 방지를 위해 가변형 _binding과 불변형 binding을 사용함
    private var _binding: FragmentConditionCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // fragment_condition_check.xml 레이아웃을 바인딩하여 뷰 생성
        _binding = FragmentConditionCheckBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parentActivity = activity as? WorkoutActivity
        val intentFolderId = parentActivity?.intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        val diaryIdStr = parentActivity?.intent?.getStringExtra("DIARY_ID") ?: "-1"

        val journals = DiaryPrefsManager.getJournals(requireContext(), intentFolderId)
        val matchedJournal = journals.find { it.id.toString() == diaryIdStr }

        val conditionList = mutableListOf<ConditionRecord>()

        if (matchedJournal != null) {
            val defaultScores = matchedJournal.conditionScores ?: listOf(10f, 10f, 10f, 10f, 10f)

            val fixedQuestions = listOf(
                StatusQuestion1(step = "1/5", title = "", score = defaultScores.getOrNull(0) ?: 10f),
                StatusQuestion1(step = "2/5", title = "", score = defaultScores.getOrNull(1) ?: 10f),
                StatusQuestion1(step = "3/5", title = "", score = defaultScores.getOrNull(2) ?: 10f),
                StatusQuestion1(step = "4/5", title = "", score = defaultScores.getOrNull(3) ?: 10f),
                StatusQuestion1(step = "5/5", title = "", score = defaultScores.getOrNull(4) ?: 10f)
            )

            conditionList.add(
                ConditionRecord(
                    title = if (matchedJournal.painTag.isNullOrEmpty()) "기록된 통증이 없습니다" else matchedJournal.painTag,
                    questions = fixedQuestions,
                    memo = matchedJournal.conditionMemo ?: "",
                    score = 0f,
                    isExpanded = true,        // 📂 대메뉴 상세 접힘은 펼쳐진 상태 유지

                    // 🎯 [전체보기 버튼 복원 완료]
                    // 여기를 false로 바꿔주어야 XML에 정의된 전체보기 버튼이 GONE 되지 않고 예쁘게 나타납니다!
                    isShowAllQuestions = false
                )
            )
            Log.d("JaehoonSync", "⭐ [조회 연동] 전체보기 버튼 보존 상태로 컨디션 셋업 완료.")
        }

        val conditionAdapter = ConditionAdapter(conditionList)
        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conditionAdapter
            itemAnimator = null
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}