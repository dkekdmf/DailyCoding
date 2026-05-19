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
 * 사용자의 운동 후 컨디션 기록 리스트를 보여주는 프래그먼트 (어댑터 수송용 시간 패킹 적용본)
 */
class ConditionCheckFragment : Fragment() {

    private var _binding: FragmentConditionCheckBinding? = null
    private val binding get() = _binding!!

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

        // 🚨 디버깅용 로그 추가
        Log.d("JaehoonDebug", "폴더ID: $intentFolderId, 일지ID: $diaryIdStr")

        val journals = DiaryPrefsManager.getJournals(requireContext(), intentFolderId)
        val matchedJournal = journals.find { it.id.toString() == diaryIdStr }

        Log.d("JaehoonDebug", "매칭된 일지 존재 여부: ${matchedJournal != null}")

        conditionList.clear()

        if (matchedJournal != null) {
            val rawPainTag = matchedJournal.painTag ?: ""
            val rawMemo = matchedJournal.conditionMemo ?: ""

            conditionList.clear()

            // 🎯 [핵심 수정] 구분자(||)가 없더라도 전체 데이터를 무조건 확인하도록 변경
            val allSessions = rawMemo.split("||").filter { it.isNotBlank() }

            for (sessionData in allSessions) {
                val parsed = parseSessionData(sessionData, rawPainTag)

                // 제목 결정
                val title = if (sessionData.contains("운동 후 상태체크")) {
                    "운동 후 상태체크 01@${parsed.time}"
                } else {
                    "컨디션 체크 ${conditionList.size + 1}@${parsed.time}"
                }

                conditionList.add(ConditionRecord(
                    title = title,
                    questions = parsed.questions,
                    memo = parsed.memo,
                    isExpanded = false
                ))
            }
        }

        conditionAdapter = ConditionAdapter(conditionList)
        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conditionAdapter
            itemAnimator = null
        }

        setEditable(false)
    }

    private fun parseSessionData(sessionData: String, rawPainTag: String): ParsedData {
        // 1. 세션 제목과 시간 추출
        val titleWithTime = sessionData.split("#")[0] // "운동 후 상태체크 01[TIME]12:34"
        val time = if (titleWithTime.contains("[TIME]")) titleWithTime.split("[TIME]")[1] else "12:34"
        val sessionTitle = titleWithTime.split("[TIME]")[0]

        // 2. [SCORE] 기준으로 메모와 점수 분리
        // 저장 구조: Title[TIME]Time#[SCORE]점수#메모
        val parts = sessionData.split("#")

        // 점수 영역 추출
        val scorePart = if (sessionData.contains("[SCORE]")) sessionData.split("[SCORE]")[1] else ""
        val scoresRaw = scorePart.split("#")[0]

        // 메모 영역 추출 ([SCORE]...# 뒤에 오는 내용)
        val memoContent = if (scorePart.contains("#")) scorePart.split("#")[1] else ""

        // 3. 통증 파싱
        val painSessions = rawPainTag.split("||")
        val matchingPain = painSessions.find { it.startsWith(sessionTitle) } ?: ""
        val painContent = if (matchingPain.contains("#")) matchingPain.split("#")[1] else "기록된 통증이 없습니다"

        val finalMemo = "통증: $painContent\n$memoContent"
        val scores = scoresRaw.split(",").mapNotNull { it.toFloatOrNull() }
        val questions = scores.map { StatusQuestion1(step = "", title = "", score = it) }

        return ParsedData(time, finalMemo, questions)
    }
    private data class ParsedData(val time: String, val memo: String, val questions: List<StatusQuestion1>)

    fun setEditable(editable: Boolean) {
        if (::conditionAdapter.isInitialized) {
            conditionAdapter.isEditMode = editable
            conditionAdapter.notifyDataSetChanged()
        }
    }
    fun saveUpdatedConditionData() {
        val parentActivity = activity as? WorkoutActivity
        // 🎯 가장 중요: WorkoutActivity에서 이미 찾아둔(matched) 일지 데이터를 가져옵니다.
        val journal = parentActivity?.matchedJournalData ?: return

        val memoBuilder = StringBuilder()
        val painBuilder = StringBuilder()

        // 1. 현재 리스트 데이터를 || 구분자로 합치기
        for (record in conditionList) {
            val titlePart = record.title.split("@").getOrNull(0) ?: "컨디션 체크"
            val timePart = record.title.split("@").getOrNull(1) ?: "12:34"
            val scorePart = record.questions.joinToString(",") { it.score.toInt().toString() }

            val lines = record.memo.split("\n")
            val painLine = lines.find { it.startsWith("통증: ") } ?: "통증: 기록된 통증이 없습니다"
            val pureMemo = lines.filter { !it.startsWith("통증: ") }.joinToString("\n")

            memoBuilder.append("$titlePart[TIME]$timePart#$pureMemo[SCORE]$scorePart||")
            painBuilder.append("$titlePart#${painLine.replace("통증: ", "")}||")
        }

        // 2. 🎯 해당 객체의 필드만 정확히 갱신
        journal.conditionMemo = memoBuilder.toString().removeSuffix("||")
        journal.painTag = painBuilder.toString().removeSuffix("||")

        // 3. 전체 리스트를 불러와서 해당 ID만 교체 후 저장 (덮어쓰기 X, 업데이트 O)
        val folderId = parentActivity.intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        val allJournals = DiaryPrefsManager.getJournals(requireContext(), folderId).toMutableList()

        val index = allJournals.indexOfFirst { it.id == journal.id }
        if (index != -1) {
            allJournals[index] = journal // 정확히 그 날짜의 일지만 교체
            DiaryPrefsManager.saveAllJournals(requireContext(), folderId, allJournals)
            Log.d("JaehoonSync", "✅ 오늘 날짜 일지(${journal.id})에 데이터 동기화 완료!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}