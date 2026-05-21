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
    val currentTime = SimpleDateFormat("HH:mm", Locale.KOREA).apply {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.condition_check)

        folderId = intent.getLongExtra("FOLDER_ID", -1L)
        targetDiaryId = intent.getStringExtra("DIARY_ID")
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
            val finalScores = questions.map { it.score }
            val inputMemo = etMemo.text.toString().trim() // 사용자가 입력한 순수 메모
            val finalPain = findViewById<TextView>(R.id.tv_pain_tag_content).text.toString()
            val scoresCsv = finalScores.joinToString(",")
            val currentTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())

            try {
                val journals = DiaryPrefsManager.getJournals(this, folderId).toMutableList()

                var targetJournal = if (targetDiaryId != null) {
                    journals.find { it.id.toString() == targetDiaryId }
                } else {
                    null
                }

                if (targetJournal == null) {
                    targetJournal = journals.lastOrNull()
                    Log.w("JaehoonSync", "⚠️ 특정 ID 매칭 실패, 가장 최근 일지에 강제 주입 시도")
                }

                if (targetJournal != null) {
                    val existingMemo = targetJournal.conditionMemo ?: ""
                    val existingPain = targetJournal.painTag ?: ""

                    val sessionCount = existingMemo.split("||").filter { it.isNotBlank() }.size + 1
                    val newIdName = String.format("컨디션체크 %02d", sessionCount)

                    // 🎯 규격화된 엔트리 생성
                    val newMemoEntry = "${newIdName}[TIME]$currentTime#$inputMemo[SCORE]$scoresCsv"
                    val newPainEntry = "${newIdName}#$finalPain"

                    // 1. 컨디션 전용 필드에 각각 누적 저장
                    targetJournal.conditionMemo = if (existingMemo.isEmpty()) newMemoEntry else "$existingMemo||$newMemoEntry"
                    targetJournal.painTag = if (existingPain.isEmpty()) newPainEntry else "$existingPain||$newPainEntry"

                    // 2. 🔥 [핵심 수술] 전체 일지 메모(memo) 필드에는 '통증 텍스트'가 절대 섞이지 않도록 가드!
                    // 오직 사용자가 et_feedback_memo에 입력한 순수 텍스트(inputMemo)만 쌓아줍니다.
                    val existingRootMemo = targetJournal.memo ?: ""
                    targetJournal.memo = if (existingRootMemo.isBlank() || existingRootMemo == "null") {
                        inputMemo
                    } else {
                        "$existingRootMemo\n$inputMemo"
                    }

                    DiaryPrefsManager.saveAllJournals(this, folderId, journals)
                    Log.d("JaehoonSync", "✅ 일지 필드 격리 저장 완료! 메모: $inputMemo | 통증: $finalPain")

                    // 부모 액티비티로 데이터 돌려주기
                    val resultIntent = Intent().apply {
                        putExtra("UPDATED_MEMO", targetJournal.memo)
                        putExtra("UPDATED_CONDITION_MEMO", targetJournal.conditionMemo)
                        putExtra("UPDATED_PAIN_TAG", targetJournal.painTag)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)

                    Toast.makeText(this, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "기록할 일지가 없습니다.", Toast.LENGTH_SHORT).show()
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
            var targetJournal = if (targetDiaryId != null) {
                journals.find { it.id.toString() == targetDiaryId }
            } else {
                null
            }

            if (targetJournal == null) {
                targetJournal = journals.lastOrNull()
            }

            targetJournal?.painTag?.let { fullPainTag ->
                if (fullPainTag.isNotBlank()) {
                    val sessions = fullPainTag.split("||").filter { it.isNotBlank() }

                    sessions.forEach { session ->
                        val content = session.split("#").getOrNull(1) ?: ""
                        if (content.isNotBlank() && !content.contains("기록된 통증이 없습니다")) {

                            val items = content.split(",")
                            items.forEach { item ->
                                val direction = item.split(":").getOrNull(0)?.trim() ?: ""

                                if (direction == "좌" || direction == "앞") { frontCount++ }
                                if (direction == "우" || direction == "뒤") { backCount++ }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("JaehoonCounting", "상단 탭 실시간 파싱 중 에러: ${e.message}")
        }

        findViewById<AppCompatButton>(R.id.btn_front)?.text = "앞면 $frontCount"
        findViewById<AppCompatButton>(R.id.btn_back)?.text = "뒷면 $backCount"
    }

    private val painTagText: TextView? get() = findViewById(R.id.tv_pain_tag_content)
}