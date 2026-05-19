package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
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
        timeZone = TimeZone.getTimeZone("Asia/Seoul") // 👈 이 한 줄이면 끝납니다!
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.condition_check)

        folderId = intent.getLongExtra("FOLDER_ID", -1L)

        initData()
        setupQuestions()
        setupBodyParts()
        setupPainTagClick()
        setupFinishButton()
    }
    private fun setupFinishButton() {
        val btnFinish = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_finish_workout)
        val etMemo = findViewById<android.widget.EditText>(R.id.et_feedback_memo)
        val tvPainTag = findViewById<TextView>(R.id.tv_pain_tag_content)

        btnFinish.setOnClickListener {
            val finalScores = questions.map { it.score }
            val inputMemo = etMemo.text.toString()
            val finalPain = tvPainTag.text.toString()

            // 🎯 [핵심 보정] KST로 확실하게 고정된 시간 객체 생성
            val kstFormatter = SimpleDateFormat("HH:mm", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }
            val currentTime = kstFormatter.format(Date()) // 👈 버튼 클릭 시점의 정확한 시간

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
            val fullDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }.format(Date())

            try {
                val prefs = getSharedPreferences("HealthDiaryPrefs", Context.MODE_PRIVATE)
                val journals = DiaryPrefsManager.getJournals(this, folderId).toMutableList()

                var todayMasterJournal = journals.find { it.createdAt.startsWith(todayDate) }
                val scoresCsv = finalScores.joinToString(",")

                if (todayMasterJournal == null) {
                    val firstIdName = "컨디션체크 01"

                    todayMasterJournal = JournalSummaryDto(
                        folderId = folderId,
                        workoutType = "오늘의 일지",
                        createdAt = fullDateTime,
                        totalTime = "00:00:00",
                        memo = "${firstIdName}[TIME]$currentTime",
                        emojiResId = R.drawable.emoticon1,
                        startTime = currentTime,
                        endTime = currentTime,
                        exerciseList = emptyList(),
                        conditionMemo = "${firstIdName}#$inputMemo",
                        painTag = "${firstIdName}#$finalPain",
                        conditionScores = finalScores
                    )
                    journals.add(todayMasterJournal)
                } else {
                    val existingFolders = todayMasterJournal.memo.split("||").filter { it.contains("컨디션체크") }
                    val nextSessionNum = existingFolders.size + 1
                    val newIdName = String.format("컨디션체크 %02d", nextSessionNum)

                    todayMasterJournal.memo = "${todayMasterJournal.memo}||${newIdName}[TIME]$currentTime"
                    todayMasterJournal.painTag = "${todayMasterJournal.painTag}||${newIdName}#$finalPain"
                    todayMasterJournal.conditionMemo = "${todayMasterJournal.conditionMemo}||${newIdName}#${inputMemo}[SCORE]$scoresCsv"
                }

                val json = com.google.gson.Gson().toJson(journals)
                prefs.edit().putString("diary_list_$folderId", json).commit()

            } catch (e: Exception) {
                Log.e("JaehoonSync", "저장 실패: ${e.message}")
            }

            setResult(Activity.RESULT_OK)
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

    // 🎯 [순정 대배너 클릭 구역 동기화] 아래 세부항목 누르는 것과 동일하게 연동
    private fun setupPainTagClick() {
        val painTagContainer = findViewById<LinearLayout>(R.id.layout_pain_tag_container)
        val painTagText = findViewById<TextView>(R.id.tv_pain_tag_content)

        painTagContainer.setOnClickListener {
            val bottomSheet = PainBottomSheetFragment("부위") { direction, stage, memo ->
                // ⭐ 요구하신 띄어쓰기 규격 통일 적용 ("좌: 부위 : 통증정도: 3단계")
                val resultText = "$direction: 부위 : 통증정도: ${stage}단계"
                painTagText.text = resultText

                // 🎨 피그마 테마 다크 컬러 매싱 (#2D3A4B)
                painTagText.setTextColor(Color.parseColor("#2D3A4B"))
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        val btnRemoveTag = painTagContainer.getChildAt(1) as? ImageView
        btnRemoveTag?.setOnClickListener {
            painTagText.text = "기록된 통증이 없습니다"
            painTagText.setTextColor(Color.parseColor("#94A3B8")) // 삭제 시 그레이 복구
        }
    }

    // 🎯 [세부 리스트 클릭 구역 완벽 패치]
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
                // ⭐ [재훈님 요청 반영] 1개 치환 방식 및 정확한 공백 레이아웃 생성
                val finalResultText = "$direction: $partString : 통증정도: ${stage}단계"

                // XML 텍스트뷰 직접 타격 교체
                val targetTextView = findViewById<TextView>(R.id.tv_pain_tag_content)
                targetTextView?.text = finalResultText

                // 🎨 파란색 걷어내고 다크 테마 컬러 적용 완료
                targetTextView?.setTextColor(Color.parseColor("#2D3A4B"))

                // 칩이 꽂혔으므로 레이아웃 박스를 강제로 노출 처리
                findViewById<LinearLayout>(R.id.layout_pain_tag_container)?.visibility = View.VISIBLE
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }
        recyclerView.adapter = bodyPartAdapter
    }

    private val painTagText: TextView? get() = findViewById(R.id.tv_pain_tag_content)
}