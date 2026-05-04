package com.example.healthcareapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.ConditionAdapter
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.StatusQuestion // 데이터 클래스 이름 확인 필요 (StatusQuestion1일 경우 수정)
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.FragmentConditionCheckBinding

class ConditionCheckFragment : Fragment() {
    private var _binding: FragmentConditionCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConditionCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 사진 기획안에 맞춘 질문 리스트 생성 함수
        // 1. 함수 반환 타입을 StatusQuestion으로 수정
        fun createHealthQuestions(category: String): List<StatusQuestion1> {
            return listOf(
                // 클래스 이름을 StatusQuestion1으로 모두 맞췄습니다.
                StatusQuestion1("1/5", "$category 통증이 있었나요?", 3f, "통증 없음", "매우 심함"),
                StatusQuestion1("2/5", "$category 강도는 적절했나요?", 4f, "너무 안 맞음", "딱 맞음"),
                StatusQuestion1("3/5", "$category 목표를 달성했나요?", 4f, "거의 못 함", "많이 함"),
                StatusQuestion1("4/5", "$category 불편함이 있었나요?", 2f, "전혀 없음", "매우 심함"),
                StatusQuestion1("5/5", "전반적인 기분 상태는?", 5f, "매우 안 좋음", "매우 개운")
            )
        }

// 2. 실제 화면에 뿌려줄 데이터 리스트 (이 부분은 인자 값이 모두 채워져 있어 정상입니다)
        val conditionList = mutableListOf(
            ConditionRecord(
                title = "컨디션 체크 01",
                questions = createHealthQuestions("전신"),
                memo = "",
                score = 0f,
                isExpanded = false,
                isShowAllQuestions = false
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
        // 3. 어댑터 설정
        val conditionAdapter = ConditionAdapter(conditionList)
        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conditionAdapter
            // 리사이클러뷰 자체의 깜빡임 방지 (선택사항)
            itemAnimator = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}