package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
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
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.fragment.DiaryMainFragment
import com.example.healthcareapp.sheet.PainBottomSheetFragment // 바텀시트 임포트 확인
import com.google.android.material.chip.ChipGroup

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.condition_check)

        initData()
        setupQuestions()
        setupBodyParts()
        setupPainTagClick()
        setupFinishButton()
    }
    private fun setupFinishButton() {
        val btnFinish = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_finish_workout)

        btnFinish.setOnClickListener {
            // 1. 필요한 경우 여기서 서버에 데이터를 저장하는 로직을 먼저 실행합니다.
            // saveConditionData()

            // 2. DiaryActivity로 이동하는 Intent 생성
            val intent = Intent(this, DiaryMainFragment::class.java)

            // 3. 현재 액티비티를 스택에서 제거하고 이동 (뒤로가기 시 다시 체크화면으로 오지 않게 함)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)

            // 4. 완료 메시지 출력 및 현재 화면 종료
//            Toast.makeText(this, "컨디션 체크가 완료되었습니다!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initData() {
        questions.clear()
        for (i in 1..5) {
            // 첫 번째 인자인 step에 맞춰 "i/5" 형식의 문자열을 넣어줍니다.
            questions.add(
                StatusQuestion1(
                    step = "$i/5",
                    title = "",
                    score = 10f
                )
            )
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
        // ⭐ R.color 값이 colors.xml에 있는지 확인하세요. 없으면 Color.parseColor("#... ") 사용
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
            // 이미 만들어두신 PainBottomSheetFragment 호출
            val bottomSheet = PainBottomSheetFragment { result ->
                // 바텀시트에서 '완료' 버튼을 눌렀을 때 실행될 로직
                // 예: 선택된 텍스트를 태그에 반영하거나 토스트 메시지 출력
                painTagText.text = result
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        // 우측 'X' 아이콘( ImageView) 클릭 시 태그 초기화 로직 (선택 사항)
        val btnRemoveTag = painTagContainer.getChildAt(1) as? ImageView
        btnRemoveTag?.setOnClickListener {
            painTagText.text = "기록된 통증이 없습니다"
            // 필요한 경우 데이터 모델에서도 삭제 처리
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

        // 1. String 리스트 가져오기
        val stringList = bodyDataMap[currentDirection]?.get(bodyKey) ?: emptyList()

        // 2. ⭐ 에러 해결: List<String>을 List<BodyPart>로 변환
        val detailList = stringList.map { BodyPart(it) }

        // 3. ⭐ 에러 해결: 어댑터 생성 시 클릭 리스너 람다 추가
        bodyPartAdapter = BodyPartAdapter(detailList) { clickedPart ->
            // 부위 클릭 시 바텀시트 띄우기
            val bottomSheet = PainBottomSheetFragment { result ->
                // 통증 기록 처리 로직
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        recyclerView.adapter = bodyPartAdapter
    }
}