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
        // 🎯 변수명을 tvPainTagContent로 명확히 지정합니다.
        val tvPainTagContent = findViewById<TextView>(R.id.tv_pain_tag_content)

        // 🎯 DIARY_ID를 Long으로 통일하여 가져옵니다.
        val diaryId = intent.getLongExtra("DIARY_ID", -1L)

        btnFinish.setOnClickListener {
            val finalScores: List<Float> = questionAdapter.getCurrentScores()
            val scoreStr = finalScores.joinToString(",") { it.toInt().toString() }

            val inputMemo = etMemo.text.toString()
            val finalPain = tvPainTagContent.text.toString()

            try {
                // 1. 전체 저널 리스트를 가져옵니다.
                val journals = DiaryPrefsManager.getJournals(this, folderId).toMutableList()
                val targetIndex = journals.indexOfFirst { it.id == diaryId }

                if (targetIndex != -1) {
                    val oldJournal = journals[targetIndex]

                    // 2. 기록 누적 로직 (기존 구현이 아주 좋습니다!)
                    val sessionCount = (oldJournal.conditionMemo?.split("||")?.filter { it.isNotBlank() }?.size ?: 0) + 1
                    val newTitle = "컨디션 체크 ${String.format("%02d", sessionCount)}"

                    val updatedMemo = if (oldJournal.conditionMemo.isNullOrBlank())
                        "$newTitle#[SCORE]$scoreStr#$inputMemo"
                    else "${oldJournal.conditionMemo}||$newTitle#[SCORE]$scoreStr#$inputMemo"

                    val updatedPain = if (oldJournal.painTag.isNullOrBlank() || oldJournal.painTag == "기록된 통증이 없습니다")
                        "$newTitle#$finalPain"
                    else "${oldJournal.painTag}||$newTitle#$finalPain"

                    // 3. 기존 객체를 복사해서 새로운 값을 덮어씁니다.
                    journals[targetIndex] = oldJournal.copy(
                        conditionMemo = updatedMemo,
                        painTag = updatedPain
                    )

                    // 🎯 직접 prefs를 수정하지 말고 DiaryPrefsManager에게 저장을 위임합니다.
                    DiaryPrefsManager.saveAllJournals(this, folderId, journals)

                    Log.d("JaehoonSync", "✅ 성공! 일지($diaryId)에 ${newTitle} 추가 완료.")
                }
            } catch (e: Exception) {
                Log.e("JaehoonSync", "저장 실패: ${e.message}")
                Toast.makeText(this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
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

        // 🎯 1. folderId를 생성자에 추가하여 전달합니다.
        // 🎯 2. questionAdapter 타입이 ConditionCheckAdapter인지 확인하세요.
        questionAdapter = ConditionCheckAdapter(questions, folderId)

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