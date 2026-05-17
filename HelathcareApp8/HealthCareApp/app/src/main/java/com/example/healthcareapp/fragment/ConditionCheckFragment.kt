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

/**
 * 사용자의 운동 후 컨디션(통증, 강도, 목표 달성 등) 기록 리스트를 보여주는 프래그먼트
 */
class ConditionCheckFragment : Fragment() {

    // ViewBinding 설정: 메모리 누수 방지를 위해 가변형 _binding과 불변형 binding을 사용함
    private var _binding: FragmentConditionCheckBinding? = null
    private val binding get() = _binding!!

    // 🎯 실시간 수정 및 바인딩 제어를 위해 어댑터를 전역 멤버 변수로 승격시킵니다.
    private lateinit var conditionAdapter: ConditionAdapter
    private val conditionList = mutableListOf<ConditionRecord>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        conditionList.clear() // 잔상 방지 초기화 가드링

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
                    isShowAllQuestions = false // 전체보기 버튼 보존 상태 유지 고정
                )
            )
            Log.d("JaehoonSync", "⭐ [조회 연동] 전체보기 버튼 보존 상태로 컨디션 셋업 완료.")
        }

        conditionAdapter = ConditionAdapter(conditionList)
        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conditionAdapter
            itemAnimator = null
        }

        // 🎯 [최초 가드링] 상세 화면 최초 진입 시점에는 무조건 '읽기 전용(수정 불가)' 상태로 자물쇠를 잠급니다.
        setEditable(false)
    }

    /**
     * 🎯 [신규 이식] 부모 액티비티(WorkoutActivity)의 수정/완료 상태에 따라 락을 온오프하는 마스터 채널
     */
    fun setEditable(editable: Boolean) {
        if (::conditionAdapter.isInitialized) {
            // 1. 외부 액티비티가 준 권한(true/false)을 어댑터 마스터 스위치에 주입
            conditionAdapter.isEditMode = editable

            // 🚨 [핵심 버그 치료] 리사이클러뷰에게 "슬라이더랑 메모장 락 풀고 다시 그려!"라고 명확히 명령
            conditionAdapter.notifyDataSetChanged()
        }
        Log.d("JaehoonEdit", "🔄 어댑터 원격 락 제어 및 화면 갱신 완료 -> 현재 수정 가능 여부: $editable")
    }

    /**
     * 🎯 [신규 이식] 부모 바의 [완료] 버튼이 터치되는 순간, 유저가 수정한 알맹이를 낚아채 디스크에 최종 커밋하는 저장 파이프라인
     */
    fun saveUpdatedConditionData() {
        val parentActivity = activity as? WorkoutActivity ?: return
        val intentFolderId = parentActivity.intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        val diaryIdStr = parentActivity.intent?.getStringExtra("DIARY_ID") ?: "-1"

        // 1. 디스크에서 현재 폴더의 일지 원본 파일 로드
        val journals = DiaryPrefsManager.getJournals(requireContext(), intentFolderId).toMutableList()
        val targetIndex = journals.indexOfFirst { it.id.toString() == diaryIdStr }

        if (targetIndex != -1) {
            val oldJournal = journals[targetIndex]

            // 2. 현재 화면의 리사이클러뷰 어댑터가 들고 있는 최종 데이터 뭉치를 낚아챕니다.
            val currentRecord = conditionList.firstOrNull() ?: return

            // 3. 기존의 운동 시간, 세트 데이터는 철통 보존하면서 수정한 메모와 스코어 배열만 쏙 덮어씁니다!
            val updatedJournal = oldJournal.copy(
                conditionMemo = currentRecord.memo,
                conditionScores = currentRecord.questions.map { it.score }
            )

            // 4. 원본 리스트 스왑 및 파일 교체 저장 완료
            journals[targetIndex] = updatedJournal
            DiaryPrefsManager.saveAllJournals(requireContext(), intentFolderId, journals)
            Log.d("JaehoonEdit", "💾 [디스크 덮어쓰기 성공] 수정한 컨디션 메모/점수 데이터가 영구 보존되었습니다.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}