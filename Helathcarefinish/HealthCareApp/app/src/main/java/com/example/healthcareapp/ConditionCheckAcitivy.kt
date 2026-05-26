package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.ConditionCheckAdapter
import com.example.healthcareapp.adapter.BodyPartAdapter
import com.example.healthcareapp.data.JournalSummaryDto
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class ConditionCheckActivity : AppCompatActivity() {

    private lateinit var questionAdapter: ConditionCheckAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter
    private val questions = mutableListOf<StatusQuestion1>()
    var currentTime = SimpleDateFormat("HH:mm", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }.format(Date())
    private val bodyDataMap = mapOf(
        "앞면" to mapOf(
            "머리/목" to listOf("머리", "이마", "얼굴", "목"),
            "상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
            "팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
            "하체" to listOf("고관절", "사타구니", "생식기", "허벅지", "무릎", "정강이"),
            "발" to listOf("발목", "발등", "발가락")
        ),
        "뒷면" to mapOf(
            "머리/목" to listOf("경추 (목뼈 부위)"),
            "상체" to listOf("등", "어깨", "날개(견갑골)", "허리", "꼬리뼈"),
            "팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
            "하체" to listOf("엉덩이", "뒷허벅지", "오금", "종아리"),
            "발" to listOf("아킬레스건", "발바닥")
        )
    )

    private var currentDirection = "앞면"
    private var folderId: Long = -1L
    private var targetDiaryId: String? = null
    private var targetDiaryDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.condition_check)
        val Close = findViewById<ImageView>(R.id.iv_close_icon)
        Close?.setOnClickListener {
            finish() // Activity를 종료하여 이전 화면으로 돌아갑니다.
        }
        folderId = intent.getLongExtra("FOLDER_ID", -1L)
        targetDiaryId = intent.getStringExtra("DIARY_ID")
        targetDiaryDate = intent.getStringExtra("DIARY_DATE")
        Log.d("JaehoonSync", "액티비티 시작됨. 전달받은 DIARY_ID: $targetDiaryId, FOLDER_ID: $folderId")

        initData()
        setupQuestions()
        setupBodyParts()
        setupPainTagClick()
        setupFinishButton()

        updateFrontBackTabCounts()
    }


    private fun setupFinishButton() {
        val btnFinish = findViewById<AppCompatButton>(R.id.btn_finish_workout)
        val etMemo = findViewById<android.widget.EditText>(R.id.et_feedback_memo)

        etMemo.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        etMemo.privateImeOptions = "nm"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            etMemo.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }

        btnFinish.setOnClickListener {

            val currentPainText = findViewById<TextView>(R.id.tv_pain_tag_content).text.toString()
            Log.d("JaehoonPain", "저장 직전 통증 텍스트 확인: '$currentPainText'")
            val finalScores = questions.map { it.score }
            val inputMemo = etMemo.text.toString().trim()
            val finalPain = findViewById<TextView>(R.id.tv_pain_tag_content).text.toString()
            val scoresCsv = finalScores.joinToString(",")

            try {
                val allJournals = DiaryPrefsManager.getJournals(this, folderId)
                val sortedJournals = allJournals.sortedByDescending { it.createdAt ?: "" }

                // 1. targetJournal 먼저 찾기
                var targetJournal = if (targetDiaryId != null) {
                    sortedJournals.find { it.id.toString() == targetDiaryId }
                } else if (targetDiaryDate != null) {
                    sortedJournals.filter { it.createdAt?.take(10) == targetDiaryDate }.firstOrNull()
                } else {
                    sortedJournals.firstOrNull()
                }

                if (targetJournal == null) {
                    Log.w("JaehoonSync", "⚠️ 매칭 실패, 가장 최근 일지 사용")
                    targetJournal = sortedJournals.firstOrNull()
                }

                if (targetJournal != null) {
                    val existingMemo = targetJournal.conditionMemo ?: ""
                    val existingPain = targetJournal.painTag ?: ""

                    // 2. 🎯 여기서 시간을 결정 (기존 세션이 있으면 마지막 시간 재사용, 없으면 현재 시간)
                    val currentTime = if (targetDiaryId != null && existingMemo.contains("[TIME]")) {
                        existingMemo.substringAfterLast("[TIME]").substringBefore("#")
                    } else {
                        val sdf = SimpleDateFormat("HH:mm", Locale.KOREA)
                        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul") // 👈 타임존을 한국으로 강제 고정!
                        sdf.format(Date())
                    }

                    // 3. 이제 안전하게 변수들 사용
                    val sessionCount = existingMemo.split("||").filter { it.isNotBlank() }.size + 1
                    val newIdName = String.format("컨디션체크 %02d", sessionCount)

                    val newMemoEntry = "${newIdName}[TIME]$currentTime#$inputMemo[SCORE]$scoresCsv"
                    val newPainEntry = "${newIdName}#$finalPain"

                    targetJournal.conditionMemo = if (existingMemo.isEmpty()) newMemoEntry else "$existingMemo||$newMemoEntry"
                    targetJournal.painTag = if (existingPain.isEmpty()) newPainEntry else "$existingPain||$newPainEntry"

                    val existingRootMemo = targetJournal.memo ?: ""
                    targetJournal.memo = if (existingRootMemo.isBlank() || existingRootMemo == "null") {
                        inputMemo
                    } else {
                        "$existingRootMemo\n$inputMemo"
                    }

                    DiaryPrefsManager.saveAllJournals(this, folderId, allJournals.toMutableList())

                    val resultIntent = Intent().apply {
                        putExtra("UPDATED_MEMO", targetJournal.memo)
                        putExtra("UPDATED_CONDITION_MEMO", targetJournal.conditionMemo)
                        putExtra("UPDATED_PAIN_TAG", targetJournal.painTag)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)

                } else {

                }
            } catch (e: Exception) {
                Log.e("JaehoonSync", "저장 실패: ${e.message}")
            }
            finish()
        }
    }
    private fun initData() {
        questions.clear()
        for (i in 1..5) {
            questions.add(StatusQuestion1(step = "$i/5", title = "", score = 10f))
        }
    }
    private fun createColoredSpannable(fullText: String, count: Int): android.text.SpannableString {
        val spannable = android.text.SpannableString(fullText)
        val startIndex = fullText.lastIndexOf(count.toString())
        if (startIndex == -1) return spannable
        val endIndex = startIndex + count.toString().length

        // 파란색 색상 적용 (#3B82F6)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(Color.parseColor("#3B82F6")),
            startIndex,
            endIndex,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun setupQuestions() {
        val rvStatus = findViewById<RecyclerView>(R.id.rv_status_questions)
        questionAdapter = ConditionCheckAdapter(questions)
        rvStatus.apply {
            layoutManager = LinearLayoutManager(this@ConditionCheckActivity)
            adapter = questionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBodyParts() {
        val btnFront = findViewById<AppCompatButton>(R.id.btn_front)
        val btnBack = findViewById<AppCompatButton>(R.id.btn_back)
        val chipGroupBody = findViewById<ChipGroup>(R.id.chip_group_body)
        val rvBodyParts = findViewById<RecyclerView>(R.id.rv_body_parts)




        btnFront.setOnClickListener {
            currentDirection = "앞면"
            updateDirectionUI(btnFront, btnBack)
            updateBodyPartList(chipGroupBody, rvBodyParts)
        }


        btnBack.setOnClickListener {
            currentDirection = "뒷면"
            updateDirectionUI(btnFront, btnBack)
            updateBodyPartList(chipGroupBody, rvBodyParts)
        }

        chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                updateBodyPartList(chipGroupBody, rvBodyParts)
            }
        }

        rvBodyParts.layoutManager = LinearLayoutManager(this)
        updateBodyPartList(chipGroupBody, rvBodyParts)
    }

    private fun updateDirectionUI(front: AppCompatButton, back: AppCompatButton) {
        val activeColor = ContextCompat.getColor(this, R.color.front_black)
        val inactiveColor = ContextCompat.getColor(this, R.color.back_gray)

        if (currentDirection == "앞면") {
            front.setBackgroundResource(R.drawable.bg_tab_selected)
            front.setTextColor(activeColor)
            back.setBackgroundResource(android.R.color.transparent)
            back.setTextColor(inactiveColor)
        } else {
            back.setBackgroundResource(R.drawable.bg_tab_selected)
            back.setTextColor(activeColor)
            front.setBackgroundResource(android.R.color.transparent)
            front.setTextColor(inactiveColor)
        }
    }
    private fun updatePainTagUI(painTagText: String?) {
        val painTagTextView = findViewById<TextView>(R.id.tv_pain_tag_content)

        if (painTagText.isNullOrBlank() || painTagText == "null" || painTagText.contains("기록된 통증이 없습니다")) {
            painTagTextView.text = "기록된 통증이 없습니다"
            painTagTextView.setTextColor(Color.parseColor("#94A3B8")) // 회색
        } else {
            painTagTextView.text = painTagText
            painTagTextView.setTextColor(Color.parseColor("#2D3A4B")) // 진한색
        }
    }
    private fun setupPainTagClick() {
        val painTagContainer = findViewById<LinearLayout>(R.id.layout_pain_tag_container)
        val painTagText = findViewById<TextView>(R.id.tv_pain_tag_content)

        painTagContainer.setOnClickListener {
            val bottomSheet = PainBottomSheetFragment("부위") { direction, stage, memo ->
                val memoSuffix = if (!memo.isNullOrBlank()) " | 메모: $memo" else ""
                val resultText = "$direction: 부위 : 통증정도: ${stage}단계$memoSuffix"

                painTagText.text = resultText
                painTagText.setTextColor(Color.parseColor("#2D3A4B"))

                updateFrontBackTabCounts()
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        val btnRemoveTag = painTagContainer.getChildAt(1) as? ImageView
        btnRemoveTag?.setOnClickListener {
            painTagText.text = "기록된 통증이 없습니다"
            painTagText.setTextColor(Color.parseColor("#94A3B8"))
            updateFrontBackTabCounts()
        }
    }

    private fun updateBodyPartList(chipGroup: ChipGroup, recyclerView: RecyclerView) {
        val selectedChipId = chipGroup.checkedChipId
        val bodyKey = when (selectedChipId) {
            R.id.chip_head -> "머리/목"
            R.id.chip_upper -> "상체"
            R.id.chip_arm -> "팔/손"
            R.id.chip_lower -> "하체"
            R.id.chip_foot -> "발"
            else -> "머리/목"
        }

        val stringList = bodyDataMap[currentDirection]?.get(bodyKey) ?: emptyList()
        val detailList = stringList.map { BodyPart(it) }

        bodyPartAdapter = BodyPartAdapter(detailList.toMutableList()) { clickedPart ->
            val partString = clickedPart.name

            val bottomSheet = PainBottomSheetFragment(partString) { direction, stage, memo ->
                val memoSuffix = if (!memo.isNullOrBlank()) " | 메모: $memo" else ""
                val finalResultText = "$direction: $partString : 통증정도: ${stage}단계$memoSuffix"

                val targetTextView = findViewById<TextView>(R.id.tv_pain_tag_content)
                targetTextView?.text = finalResultText
                targetTextView?.setTextColor(Color.parseColor("#2D3A4B"))

                findViewById<LinearLayout>(R.id.layout_pain_tag_container)?.visibility = View.VISIBLE

                updateFrontBackTabCounts()
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }
        recyclerView.adapter = bodyPartAdapter
    }

    private fun updateFrontBackTabCounts() {
        var frontCount = 0
        var backCount = 0

        try {
            val journals = DiaryPrefsManager.getJournals(this, folderId)
            val targetJournal = targetDiaryId?.let { id -> journals.find { it.id.toString() == id } } ?: journals.lastOrNull()

            targetJournal?.painTag?.let { fullPainTag ->
                val sessions = fullPainTag.split("||").filter { it.isNotBlank() }

                // 💡 핵심: 세션별로 딱 한 번씩만 검사하도록 구조화
                sessions.forEach { session ->
                    val content = session.split("#").getOrNull(1) ?: ""
                    if (content.isNotBlank() && !content.contains("기록된 통증이 없습니다")) {

                        // 각 세션 데이터에 "앞" 혹은 "좌"가 있으면 앞면 카운트 +1
                        // "뒤" 혹은 "우"가 있으면 뒷면 카운트 +1
                        // 이렇게 하면 "앞10"처럼 데이터가 꼬여있어도 딱 1번만 체크됩니다.
                        if (content.contains("앞") || content.contains("좌")) {
                            frontCount++
                        } else if (content.contains("뒤") || content.contains("우")) {
                            backCount++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("JaehoonCounting", "파싱 에러: ${e.message}")
        }

        // UI 적용
        findViewById<AppCompatButton>(R.id.btn_front)?.text = createColoredSpannable("앞면 $frontCount", frontCount)
        findViewById<AppCompatButton>(R.id.btn_back)?.text = createColoredSpannable("뒷면 $backCount", backCount)
    }

    private val painTagText: TextView? get() = findViewById(R.id.tv_pain_tag_content)
}