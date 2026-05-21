package com.example.healthcareapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.DiaryPrefsManager
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.adapter.ConditionAdapter
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.FragmentConditionCheckBinding

class ConditionCheckFragment : Fragment() {

    private var _binding: FragmentConditionCheckBinding? = null
    private val binding get() = _binding!!

    var conditionAdapter: ConditionAdapter? = null
    val conditionList = mutableListOf<ConditionRecord>()

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

        conditionList.clear()
        if (matchedJournal != null) {
            val rawPainTag = matchedJournal.painTag ?: ""
            val rawMemo = matchedJournal.conditionMemo ?: ""

            updatePainTagUI(rawPainTag)

            val allSessions = rawMemo.split("||").filter { it.isNotBlank() }

            for ((index, sessionData) in allSessions.withIndex()) {
                // 🎯 [수술 완료] 통증 태그 원본을 파싱 파이프라인에 동시 태웁니다.
                val parsed = parseSessionData(sessionData, rawPainTag)

                val title = if (sessionData.contains("운동 후 상태체크")) {
                    "운동 후 상태체크 01"
                } else {
                    "컨디션 체크 ${index + 1}"
                }

                // 🎯 [수술 완료] 누락되었던 painTag 자리에 쪼개진 순수 통증 본문을 매핑 주입합니다.
                conditionList.add(ConditionRecord(
                    title = title,
                    questions = parsed.questions,
                    time = parsed.time,
                    memo = parsed.memo,      // 👈 오염되지 않은 순수한 유저 메모 ("123123")
                    painTag = parsed.painTag, // 👈 제자리 찾은 순수 통증 태그 ("좌 팔꿈치...")
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
        // 1. 시간 파싱
        val titleWithTime = sessionData.split("#")[0]
        val rawTime = if (titleWithTime.contains("[TIME]")) titleWithTime.split("[TIME]")[1] else "12:34"
        val sessionTitle = titleWithTime.split("[TIME]")[0]

        val finalTime = if (rawTime.contains(":") && !rawTime.contains("오전") && !rawTime.contains("오후")) {
            try {
                val timeParts = rawTime.split(":")
                val rawHour = timeParts[0].toIntOrNull() ?: 12
                val rawMinute = timeParts[1].toIntOrNull() ?: 34

                var correctedHour = rawHour - 3
                if (correctedHour < 0) correctedHour += 24

                var hour12 = correctedHour % 12
                if (hour12 == 0) hour12 = 12
                val amPmText = if (correctedHour < 12) "오전" else "오후"

                String.format(java.util.Locale.KOREAN, "%s %02d:%02d", amPmText, hour12, rawMinute)
            } catch (e: Exception) {
                rawTime
            }
        } else {
            rawTime
        }

        // 2. [SCORE] 기준으로 본문 내용 분석
        // 🚨 기존 구조: 제목[TIME]시간#메모내용[SCORE]점수
        val bodyPart = if (sessionData.contains("#")) sessionData.split("#").getOrNull(1) ?: "" else ""

        val pureMemo = if (bodyPart.contains("[SCORE]")) {
            bodyPart.split("[SCORE]")[0]
        } else {
            bodyPart
        }.trim()

        val scorePart = if (bodyPart.contains("[SCORE]")) bodyPart.split("[SCORE]")[1] else ""
        val scoresRaw = scorePart.split("#")[0]

        // 3. 🎯 [철벽 가드] 통증 데이터 분리 추출 (메모에 절대 섞지 않고 독자 노출)
        val painSessions = rawPainTag.split("||")
        val matchingPain = painSessions.find { it.startsWith(sessionTitle) } ?: ""
        val painContent = if (matchingPain.contains("#")) matchingPain.split("#")[1] else "기록된 통증이 없습니다"

        val scores = scoresRaw.split(",").mapNotNull { it.toFloatOrNull() }
        val questions = scores.map { StatusQuestion1(step = "", title = "", score = it) }

        // 🎯 순수 메모와 순수 통증 태그를 꼬임 없이 깔끔하게 독립 반환!
        return ParsedData(finalTime, pureMemo, painContent, questions)
    }

    private data class ParsedData(
        val time: String,
        val memo: String,
        val painTag: String,
        val questions: List<StatusQuestion1>
    )

    fun setEditable(editable: Boolean) {
        conditionAdapter?.let {
            it.isEditMode = editable
            it.notifyDataSetChanged()
        }
    }

    fun saveUpdatedConditionData() {
        val parentActivity = activity as? WorkoutActivity
        val journal = parentActivity?.matchedJournalData ?: return

        val memoBuilder = StringBuilder()
        val painBuilder = StringBuilder()

        // 🎯 [수술 완료] 유저가 줄바꿈 엔터를 쳐도 깨지지 않도록 record 객체 내부의 독립 필드 그대로 조립 보관합니다.
        for (record in conditionList) {
            val titlePart = record.title
            val timePart = record.time
            val scorePart = record.questions.joinToString(",") { it.score.toInt().toString() }

            val pureMemo = record.memo.trim()
            val purePain = record.painTag.trim()

            memoBuilder.append("$titlePart[TIME]$timePart#$pureMemo[SCORE]$scorePart||")
            painBuilder.append("$titlePart#$purePain||")
        }

        journal.conditionMemo = memoBuilder.toString().removeSuffix("||")
        journal.painTag = painBuilder.toString().removeSuffix("||")

        val folderId = parentActivity.intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        val allJournals = DiaryPrefsManager.getJournals(requireContext(), folderId).toMutableList()

        val index = allJournals.indexOfFirst { it.id == journal.id }
        if (index != -1) {
            allJournals[index] = journal
            DiaryPrefsManager.saveAllJournals(requireContext(), folderId, allJournals)
            Log.d("JaehoonSync", "✅ 필드 크래시 없이 격리 동기화 완료")
        }
    }

    private fun updatePainTagUI(painData: String) {
        val container = binding.root.findViewById<LinearLayout>(R.id.layout_pain_tag_container)
        val textView = binding.root.findViewById<TextView>(R.id.tv_pain_tag_content)

        if (container != null && textView != null) {
            if (painData.isNotEmpty() && painData != "기록된 통증이 없습니다") {
                textView.text = painData
                container.visibility = View.VISIBLE
            } else {
                container.visibility = View.GONE
            }
        }
    }

    fun updateAllItemsTime(newTime: String) {
        if (conditionAdapter == null) return
        try {
            for (i in 0 until conditionList.size) {
                conditionList[i].time = newTime
            }
            conditionAdapter?.notifyDataSetChanged()
            Log.d("JaehoonFragment", "🔥 리스트 시간 전면 교체 완료: $newTime")
        } catch (e: Exception) {
            Log.e("JaehoonFragment", "시간 변경 오류 방어: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}