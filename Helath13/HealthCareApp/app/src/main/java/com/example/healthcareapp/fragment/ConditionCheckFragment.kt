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
            val painData = matchedJournal.painTag
            if (!painData.isNullOrEmpty() && painData != "기록된 통증이 없습니다") {
                binding.root.findViewById<LinearLayout>(R.id.layout_pain_tag_container)?.visibility = View.VISIBLE
                binding.root.findViewById<TextView>(R.id.tv_pain_tag_content)?.text = painData
            } else {
                binding.root.findViewById<LinearLayout>(R.id.layout_pain_tag_container)?.visibility = View.GONE
            }

            val rawPainTag = matchedJournal.painTag ?: ""
            val rawMemo = matchedJournal.conditionMemo ?: ""

            updatePainTagUI(rawPainTag)

            val allSessions = rawMemo.split("||").filter { it.isNotBlank() }

            // 🎯 [교정] 인덱스 기반으로 꼬임 없이 순회하며 데이터 적재
            for ((index, sessionData) in allSessions.withIndex()) {
                val parsed = parseSessionData(sessionData, rawPainTag)

                val title = if (sessionData.contains("운동 후 상태체크")) {
                    "운동 후 상태체크 01"
                } else {
                    "컨디션 체크 ${index + 1}"
                }

                // 🎯 타이틀과 시간을 완벽하게 분리하여 저장
                conditionList.add(ConditionRecord(
                    title = title,
                    questions = parsed.questions,
                    time = parsed.time, // 👈 생성한 time 변수에 안전하게 전달
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
        // 1. 세션 제목과 시간 추출 (예: "운동 후 상태체크 01[TIME]21:56" 이 들어옵니다)
        val titleWithTime = sessionData.split("#")[0]
        val rawTime = if (titleWithTime.contains("[TIME]")) titleWithTime.split("[TIME]")[1] else "12:34"
        val sessionTitle = titleWithTime.split("[TIME]")[0]

        // 🎯 [시차 오차 원천 박멸] 파일에서 긁어온 시간이 24시간제 찌꺼기(예: 21:56)면 불러오는 순간 바로 3시간을 뒤로 뺍니다!
        val finalTime = if (rawTime.contains(":") && !rawTime.contains("오전") && !rawTime.contains("오후")) {
            try {
                val timeParts = rawTime.split(":")
                val rawHour = timeParts[0].toIntOrNull() ?: 12
                val rawMinute = timeParts[1].toIntOrNull() ?: 34

                // 💥 여기서 무조건 3시간을 뺍니다! (21시 - 3 = 18시)
                var correctedHour = rawHour - 3
                if (correctedHour < 0) correctedHour += 24 // 0시보다 작아지면 전날로 보정

                // 12시간제 숫자로 변환 (18 % 12 = 6시)
                var hour12 = correctedHour % 12
                if (hour12 == 0) hour12 = 12
                val amPmText = if (correctedHour < 12) "오전" else "오후"

                // "오후 06:56" 형태로 가공 완료
                String.format(java.util.Locale.KOREAN, "%s %02d:%02d", amPmText, hour12, rawMinute)
            } catch (e: Exception) {
                rawTime // 혹시 에러 나면 안전 장치로 원본 노출
            }
        } else {
            rawTime // 이미 포맷팅된 형태면 그대로 노출
        }

        // 2. [SCORE] 기준으로 메모와 점수 분리
        val scorePart = if (sessionData.contains("[SCORE]")) sessionData.split("[SCORE]")[1] else ""
        val scoresRaw = scorePart.split("#")[0]
        val memoContent = if (scorePart.contains("#")) scorePart.split("#")[1] else ""

        // 3. 통증 파싱
        val painSessions = rawPainTag.split("||")
        val matchingPain = painSessions.find { it.startsWith(sessionTitle) } ?: ""
        val painContent = if (matchingPain.contains("#")) matchingPain.split("#")[1] else "기록된 통증이 없습니다"

        val finalMemo = "통증: $painContent\n$memoContent"
        val scores = scoresRaw.split(",").mapNotNull { it.toFloatOrNull() }
        val questions = scores.map { StatusQuestion1(step = "", title = "", score = it) }

        // 🎯 가공된 finalTime("오후 06:56")을 반환합니다.
        return ParsedData(finalTime, finalMemo, questions)
    }

    private data class ParsedData(val time: String, val memo: String, val questions: List<StatusQuestion1>)

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

        for (record in conditionList) {
            val titlePart = record.title
            val timePart = record.time
            val scorePart = record.questions.joinToString(",") { it.score.toInt().toString() }

            val lines = record.memo.split("\n")
            val painLine = lines.find { it.startsWith("통증: ") } ?: "통증: 기록된 통증이 없습니다"
            val pureMemo = lines.filter { !it.startsWith("통증: ") }.joinToString("\n")

            memoBuilder.append("$titlePart[TIME]$timePart#$pureMemo[SCORE]$scorePart||")
            painBuilder.append("$titlePart#${painLine.replace("통증: ", "")}||")
        }

        journal.conditionMemo = memoBuilder.toString().removeSuffix("||")
        journal.painTag = painBuilder.toString().removeSuffix("||")

        val folderId = parentActivity.intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        val allJournals = DiaryPrefsManager.getJournals(requireContext(), folderId).toMutableList()

        val index = allJournals.indexOfFirst { it.id == journal.id }
        if (index != -1) {
            allJournals[index] = journal
            DiaryPrefsManager.saveAllJournals(requireContext(), folderId, allJournals)
            Log.d("JaehoonSync", "✅ 이모티콘 오염 없이 컨디션 데이터만 안전하게 동기화 완료")
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

    // 🎯 [완벽 교정] 찌꺼기 없는 깔끔한 다이렉트 시간 주입 원격 함수
    fun updateAllItemsTime(newTime: String) {
        if (conditionAdapter == null) return
        try {
            // 리스트의 모든 아이템에 새로 생성된 시간을 독립 변수에 바로 꽂습니다.
            for (i in 0 until conditionList.size) {
                conditionList[i].time = newTime
            }
            conditionAdapter?.notifyDataSetChanged()
            Log.d("JaehoonFragment", "🔥 꼬임 현상 없이 리스트 시간 원격 전면 교체 완료: $newTime")
        } catch (e: Exception) {
            Log.e("JaehoonFragment", "시간 변경 오류 방어: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}