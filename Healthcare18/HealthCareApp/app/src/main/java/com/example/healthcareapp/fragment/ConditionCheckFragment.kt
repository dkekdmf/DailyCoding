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
import com.example.healthcareapp.adapter.ConditionCheckAdapter
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
        val timerContainer = activity?.findViewById<View>(R.id.layout_timer_container)
        timerContainer?.visibility = View.GONE
        conditionList.clear()
        if (matchedJournal != null) {
            val rawPainTag = matchedJournal.painTag ?: ""
            val rawMemo = matchedJournal.conditionMemo ?: ""

            updatePainTagUI(rawPainTag)
// ... 기존 코드 (journals 불러오기 등) ...

            val allSessions = rawMemo.split("||").filter { it.isNotBlank() }

            for ((index, sessionData) in allSessions.withIndex()) {
                val parsed = parseSessionData(sessionData, rawPainTag)

                // 🎯 [핵심 로직] 제목 규칙 재정의
                // 1. "운동 후" 키워드가 포함되어 있으면 무조건 "운동 후 컨디션 체크"
                // 2. 그 외에는 "컨디션 체크 01", "컨디션 체크 02"... 순차 부여
                val title = if (sessionData.contains("운동 후")) {
                    "운동 후 컨디션 체크"
                } else {
                    // 전체 세션 중 "운동 후"가 아닌 항목들만 카운팅하여 번호 매기기
                    val pureConditionIndex = allSessions.filter { !it.contains("운동 후") }
                        .indexOf(sessionData) + 1
                    String.format("컨디션 체크 %02d", pureConditionIndex)
                }

                conditionList.add(ConditionRecord(
                    title = title,
                    questions = parsed.questions,
                    time = parsed.time,
                    memo = parsed.memo,
                    painTag = parsed.painTag,
                    isExpanded = false
                ))
            }
// ... 나머지 코드 ...
        }
        binding.root.post {
            // RecyclerView의 최상단 마진을 0으로 설정
            val params = binding.rvConditionList.layoutParams as? ViewGroup.MarginLayoutParams
            params?.topMargin = 0
            binding.rvConditionList.layoutParams = params

            // 레이아웃 전체 패딩 제거 (만약 부모 레이아웃에 패딩이 있다면)
            binding.root.setPadding(0, 0, 0, 0)


        }
        loadData()
        conditionAdapter = ConditionAdapter(conditionList)
        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conditionAdapter
            itemAnimator = null
        }

        setEditable(false)
    }
    override fun onResume() {
        super.onResume()
        // 🎯 [핵심] 탭을 눌러서 다시 돌아올 때마다 데이터를 다시 불러옴
        loadData()
    }
    private fun parseSessionData(sessionData: String, rawPainTag: String): ParsedData {
        val titleWithTime = sessionData.split("#")[0]
        val sessionTitle = titleWithTime.split("[TIME]")[0]

        // 1. 시간 파싱 (안전하게 처리)
        val rawTime = if (titleWithTime.contains("[TIME]")) titleWithTime.split("[TIME]")[1] else "12:00"
        val finalTime = rawTime

        // 2. 본문 및 스코어 분석 (기존 로직 유지하되 안전성 강화)
        val bodyPart = if (sessionData.contains("#")) sessionData.substringAfter("#") else ""
        val pureMemo = bodyPart.substringBefore("[SCORE]").trim()
        val scoresRaw = if (bodyPart.contains("[SCORE]")) bodyPart.substringAfter("[SCORE]").split("||")[0] else ""

        // 3. 통증 데이터 매핑
        val painSessions = rawPainTag.split("||")
        val matchingPain = painSessions.find { it.startsWith(sessionTitle) } ?: ""
        val painContent = if (matchingPain.contains("#")) matchingPain.substringAfter("#") else "기록된 통증이 없습니다"

        val scores = scoresRaw.split(",").mapNotNull { it.toFloatOrNull() }
        val questions = scores.map { StatusQuestion1(step = "", title = "", score = it) }

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
            // 🎯 [핵심] 어댑터가 즉시 UI를 다시 그리도록 강제
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

            // 🎯 [핵심] record.time은 데이터 로드 시 이미 파싱된 문자열입니다.
            // 여기에 절대로 Date()나 SimpleDateFormat을 적용하지 마세요!
            // 저장 시에는 로드했던 그 문자열을 그대로 '문자열 복사'만 합니다.
            val timePart = record.time

            val scorePart = record.questions.joinToString(",") { it.score.toInt().toString() }
            val pureMemo = record.memo.trim()
            val purePain = record.painTag.trim()

            // 🎯 여기서 timePart가 변하지 않도록 강제합니다.
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

    fun refreshData() {
        loadData() // 아까 만든 데이터 로드 함수
        conditionAdapter?.notifyDataSetChanged()
    }
    private fun loadData() {
        val parentActivity = activity as? WorkoutActivity
        val intentFolderId = parentActivity?.intent?.getLongExtra("FOLDER_ID", -1L) ?: -1L
        val diaryIdStr = parentActivity?.intent?.getStringExtra("DIARY_ID") ?: "-1"

        val journals = DiaryPrefsManager.getJournals(requireContext(), intentFolderId)
        val matchedJournal = journals.find { it.id.toString() == diaryIdStr }

        // 1. 임시 리스트에 데이터 로드
        val newConditionList = mutableListOf<ConditionRecord>()

        if (matchedJournal != null) {
            val rawPainTag = matchedJournal.painTag ?: ""
            val rawMemo = matchedJournal.conditionMemo ?: ""

            updatePainTagUI(rawPainTag)

            val allSessions = rawMemo.split("||").filter { it.isNotBlank() }
            for (sessionData in allSessions) {
                val parsed = parseSessionData(sessionData, rawPainTag)

                val title = if (sessionData.contains("운동 후")) "운동 후 컨디션 체크"
                else String.format("컨디션 체크 %02d", allSessions.filter { !it.contains("운동 후") }.indexOf(sessionData) + 1)

                newConditionList.add(ConditionRecord(
                    title = title,
                    questions = parsed.questions,
                    time = parsed.time,
                    memo = parsed.memo,
                    painTag = parsed.painTag,
                    isExpanded = false
                ))
            }
        }

        // 2. 기존 conditionList 업데이트
        conditionList.clear()
        conditionList.addAll(newConditionList)

        // 3. 🎯 [핵심] 어댑터가 있으면 새로 만들지 말고 데이터만 갱신
        if (conditionAdapter == null) {
            conditionAdapter = ConditionAdapter(conditionList)
            binding.rvConditionList.adapter = conditionAdapter
        } else {
            // 어댑터가 이미 있다면 데이터는 공유하므로 notify만 호출
            conditionAdapter?.notifyDataSetChanged()
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