package com.example.healthcareapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.ConditionAdapter
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.FragmentConditionCheckBinding

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

        /**
         * 1. 질문 리스트 생성 내부 함수
         * @param category 전신, 운동, 기타 등 질문의 머리말에 들어갈 카테고리명
         * @return 각 항목에 맞는 5가지 고정 질문 리스트를 반환
         */
        fun createHealthQuestions(category: String): List<StatusQuestion1> {
            return listOf(
                StatusQuestion1("1/5", "$category 통증이 있었나요?", 3f, "통증 없음", "매우 심함"),
                StatusQuestion1("2/5", "$category 강도는 적절했나요?", 4f, "너무 안 맞음", "딱 맞음"),
                StatusQuestion1("3/5", "$category 목표를 달성했나요?", 4f, "거의 못 함", "많이 함"),
                StatusQuestion1("4/5", "$category 불편함이 있었나요?", 2f, "전혀 없음", "매우 심함"),
                StatusQuestion1("5/5", "전반적인 기분 상태는?", 5f, "매우 안 좋음", "매우 개운")
            )
        }

        /**
         * 2. 실제 화면에 뿌려줄 데이터 리스트 구성 (임시 데이터)
         * - ConditionRecord: 하나의 컨디션 체크 단위 (제목, 질문들, 메모, 확장 상태 등 포함)
         */
        val conditionList = mutableListOf(
            ConditionRecord(
                title = "컨디션 체크 01",
                questions = createHealthQuestions("전신"),
                memo = "",
                score = 0f,
                isExpanded = false, // 리스트 아이템이 펼쳐져 있는지 여부
                isShowAllQuestions = false // 모든 질문을 다 보여줄지 여부
            ),
            ConditionRecord(
                title = "운동 후 컨디션 체크",
                questions = createHealthQuestions("운동"),
                memo = "",
                score = 0f,
                isExpanded = false,
                isShowAllQuestions = false
            ),
            ConditionRecord(
                title = "컨디션 체크 02",
                questions = createHealthQuestions("기타"),
                memo = "",
                score = 0f,
                isExpanded = false,
                isShowAllQuestions = false
            )
        )

        // 3. 리사이클러뷰 어댑터 설정 및 연결
        val conditionAdapter = ConditionAdapter(conditionList)
        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conditionAdapter

            // ⭐ 아이템 변경 시 불필요한 깜빡임 애니메이션을 제거하여 사용자 경험 개선
            itemAnimator = null
        }
    }

    /**
     * 프래그먼트의 뷰가 파괴될 때 바인딩 참조를 해제하여 메모리 누수 방지
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}