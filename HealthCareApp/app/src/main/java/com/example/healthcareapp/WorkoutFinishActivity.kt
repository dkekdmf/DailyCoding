package com.example.healthcareapp


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.StatusQuestionAdapter
import com.example.healthcareapp.adapter.BodyPartAdapter

import com.example.healthcareapp.data.CreateJournalRequest
import com.example.healthcareapp.data.CreateJournalResponse

import com.example.healthcareapp.data.JournalApiResponse
import com.example.healthcareapp.data.JournalDetailDto
import com.example.healthcareapp.data.PreConditionDto

import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.StatusQuestionBinding
import com.example.healthcareapp.network.RetrofitClient
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import com.example.zero.healthcare.dto.journal.CompleteJournalRequest
import com.example.zero.healthcare.dto.journal.PostConditionDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * 운동 종료 후 요약 정보 표시 및 컨디션/통증 기록 화면
 */
class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: StatusQuestionBinding
    private lateinit var questionAdapter: StatusQuestionAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter

    private lateinit var questionList: List<StatusQuestion>

    private var receivedWorkoutType: String = "개인운동"
    private var selectedEmojiId: Int = R.drawable.emoticon1 // ⭐ [에러해결] 클래스 변수로 선언

    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private val bodyPartMap = mapOf(
        "FRONT_머리/목" to listOf("머리", "이마", "얼굴", "목"),
        "FRONT_상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
        "FRONT_팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
        "FRONT_하체" to listOf("고관절", "사타구니", "생식기", "허벅지", "무릎", "정강이"),
        "FRONT_발" to listOf("발목", "발등", "발가락"),

        "BACK_머리/목" to listOf("경추 (목뼈 부위)"),
        "BACK_상체" to listOf("등", "어깨", "날개(견갑골)", "허리", "꼬리뼈"),
        "BACK_팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
        "BACK_하체" to listOf("엉덩이", "뒷허벅지", "오금", "종아리"),
        "BACK_발" to listOf("아킬레스건", "발바닥")
    )

    private var currentDirection = "FRONT"
    private var currentCategory = "머리/목"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatusQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receivedWorkoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "개인운동"
        android.util.Log.d("JaehoonTest", "WorkoutFinishActivity onCreate - 받은 타입: $receivedWorkoutType")

        setupTimeInfo()
        setupStatusQuestions()
        setupBodyParts()
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.layoutPainTagContainer.setOnClickListener {
            val bottomSheet = PainBottomSheetFragment { selectedPainInfo ->
                binding.tvPainTagContent.text = selectedPainInfo
                binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF"))
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        binding.btnFinishWorkout.setOnClickListener {
            // ⭐ [수정] 단순히 finish()하지 않고 서버 저장 함수를 호출합니다.
            saveWorkoutJournal()
        }
    }

    private fun setupTimeInfo() {
        val totalTime = intent.getStringExtra("TOTAL_TIME") ?: "00:00:00"
        val startTime = intent.getStringExtra("START_TIME") ?: "00:00"
        val endTime = intent.getStringExtra("END_TIME") ?: "00:00"

        binding.tvTotalTime.text = totalTime
        binding.tvStartTime.text = "시작\n$startTime"
        binding.tvEndTime.text = "종료\n$endTime"
    }

    private fun setupStatusQuestions() {
        questionList = listOf(
            StatusQuestion(1, "운동 후 평소와 다른 관절이나\n근육 통증이 있었나요?", "매우 심함", "통증 없음", 10,
                mapOf(
                    1 to "1 - 매우 심함 / 일상 움직임도 힘들 정도",
                    2 to "2 - 통증이 많이 심한 편임",
                    3 to "3 - 통증이 뚜렷하고 불편함이 큼",
                    4 to "4 - 통증이 꽤 느껴지고 거슬림",
                    5 to "5 - 통증이 분명히 느껴짐",
                    6 to "6 - 약간 신경 쓰이는 통증이 있음",
                    7 to "7 - 가벼운 불편감이 있음",
                    8 to "8 - 아주 약하게 느껴짐",
                    9 to "9 - 거의 느껴지지 않음",
                    10 to "10 - 통증 없음"
                )),
            StatusQuestion(2, "오늘 운동 강도는 내 몸 상태에\n적절했나요?", "너무 약하거나 무리", "딱 맞았음", 8,
                mapOf(
                    1 to "1 - 너무 약하거나 무리 / 호흡, 근육, 자세 등이 안 맞음",
                    2 to "2 - 많이 안 맞음 / 너무 쉽거나 너무 버거움",
                    3 to "3 - 안 맞는 편 / 숨이 너무 차거나 자극이 부족했음",
                    4 to "4 - 조금 아쉬움 / 강도가 다소 안 맞았음",
                    5 to "5 - 무난하지만 애매 / 숨참이나 근육 피로가 부족하거나 과했음",
                    6 to "6 - 크게 무리 없음 / 전반적으로 소화 가능했음",
                    7 to "7 - 대체로 잘 맞음 / 숨은 차지만 자세는 유지됨",
                    8 to "8 - 잘 맞음 / 근육 피로와 호흡이 적절했음",
                    9 to "9 - 매우 잘 맞음 / 힘들었지만 끝까지 안정적으로 수행함",
                    10 to "10 - 딱 맞음 / 숨참, 근육 피로, 자세 유지가 모두 적절했음"
                )),
            StatusQuestion(3, "운동 후 어지러움이나\n불편감이 있었나요?", "매우 심했음", "전혀 없었음", 10,
                mapOf(
                    1 to "1 - 매우 심했음 / 움직이기 어렵고 오래 불편했음",
                    2 to "2 - 많이 심했음 / 한참 쉬어야 했음",
                    3 to "3 - 심한 편이었음 / 바로 회복되지 않았음",
                    4 to "4 - 꽤 불편했음 / 잠시 멈추고 쉬고 싶었음",
                    5 to "5 - 분명히 느껴졌음 / 신경 쓰일 정도였음",
                    6 to "6 - 조금 있었음 / 잠깐 불편했음",
                    7 to "7 - 약하게 있었음 / 금방 괜찮아졌음",
                    8 to "8 - 아주 미세했음 / 거의 신경 쓰이지 않았음",
                    9 to "9 - 거의 없었음",
                    10 to "10 - 전혀 없었음"
                )),
            StatusQuestion(4, "운동 후 전반적인 기분 상태는\n어떤가요?", "매우 안 좋음", "최상 ", 7,
                mapOf(
                    1 to "1 - 매우 안 좋음 / 많이 지치고 힘든 상태",
                    2 to "2 - 많이 안 좋은 상태",
                    3 to "3 - 안 좋은 편 / 피로감이 큼",
                    4 to "4 - 다소 안 좋은 상태",
                    5 to "5 - 보통 이하 / 썩 좋지 않음",
                    6 to "6 - 무난한 상태",
                    7 to "7 - 괜찮은 편 / 비교적 안정적임",
                    8 to "8 - 좋은 편 / 몸과 마음이 가벼운 편임",
                    9 to "9 - 매우 좋음 / 활력이 있음",
                    10 to "10 - 최상 / 매우 개운하고 만족스러움"
                )),
            StatusQuestion(5, "오늘 계획한 운동 목표를\n달성했나요?", "거의 못 함", "계획보다 많이 더 함", 8,
                mapOf(
                    1 to "1 - 거의 못 함",
                    2 to "2 - 조금만 함",
                    3 to "3 - 일부만 함",
                    4 to "4 - 절반도 못 함",
                    5 to "5 - 절반 정도 함",
                    6 to "6 - 절반 넘게 함",
                    7 to "7 - 대부분 함",
                    8 to "8 - 계획한 만큼 함",
                    9 to "9 - 계획보다 조금 더 함",
                    10 to "10 - 계획보다 많이 더 함"
                ))
        )

        questionAdapter = StatusQuestionAdapter(questionList)
        binding.rvStatusQuestions.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = questionAdapter
        }
    }
    private fun saveWorkoutJournal() {
        val startTime = intent.getStringExtra("START_TIME") ?: ""
        Log.d("JaehoonLog", "saveWorkoutJournal 시작 - 프래그먼트에서 전달받은 startTime: $startTime")
        binding.btnFinishWorkout.isEnabled = false

        // 🚨 [핵심 수정] 타임존이 적용된 서울 표준시 기준 포맷터를 생성합니다.
        val sdfFullDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val sdfDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }

        val now = Date()
        val datePart = sdfDateOnly.format(now) // "2026-05-14" 등 한국 시각 기준 날짜 추출

        // 프래그먼트에서 넘겨준 "00:05:20" 형태의 정확한 한국 시각 문자열을 결합합니다.
        val finalStartedAt = try {
            if (startTime.isNotEmpty() && startTime.contains(":")) {
                // 초 단위가 생략되어 "00:05"로 올 경우를 대비한 방어 코드
                val timePart = if (startTime.length == 5) "$startTime:00" else startTime
                "${datePart}T$timePart" // "2026-05-14T00:05:20" 구조 완성
            } else {
                sdfFullDateTime.format(now)
            }
        } catch (e: Exception) {
            sdfFullDateTime.format(now)
        }

        // 질문 리스트 점수를 안전하게 가져옵니다 (1~10 제한)
        fun validateScore(index: Int, default: Int): Int {
            val rawScore = questionList.getOrNull(index)?.score?.toInt() ?: default
            return rawScore.coerceIn(1, 10)
        }

        // 점수 범위(1~10)를 절대 벗어나지 않도록 강제 방어하는 헬퍼 함수
        fun validatePostScore(index: Int, default: Int): Int {
            val rawScore = questionList.getOrNull(index)?.score?.toInt() ?: default
            return rawScore.coerceIn(1, 10) // 1 미만은 1, 10 초과는 10으로 고정
        }

        // 매핑 안전 조치: 각 점수가 유효 범위 안에 있는지 확실히 검증합니다.
        val postCondition = PostConditionDto(
            jointMusclePain = 5,
            intensityFit = 5,
            goalAchieved = 5,
            dizziness = 5, // 1~10 사이 고정
            mood = 5
        )

        // 최종 요청 객체 생성
        val request = CompleteJournalRequest(
            workoutDate = datePart,
            startedAt = finalStartedAt, // 👈 이제 여기에 15시 대신 진짜 '00시' 데이터가 주입됩니다!
            totalDurationSeconds = 3600,
            folderId = 1,
            postCondition = postCondition,
            painRecords = emptyList(),
            exercises = emptyList(),
            content = "",
            imageUrls = emptyList()
        )

        Log.d("JaehoonLog", "🚀 [최종 백엔드 전송 데이터] 시작시간(startedAt): ${request.startedAt}, 날짜(workoutDate): ${request.workoutDate}")

        // Retrofit 통신 요청
        RetrofitClient.journalService.completeJournal(request).enqueue(object : Callback<JournalApiResponse<JournalDetailDto>> {
            override fun onResponse(call: Call<JournalApiResponse<JournalDetailDto>>, response: Response<JournalApiResponse<JournalDetailDto>>) {
                if (response.isSuccessful) {
                    Log.d("JaehoonLog", "🎉 대성공!! 운동 완료 일지 저장 완료!")

                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()

                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e("JaehoonLog", "실패 코드: ${response.code()} / 에러 내용: $errorMsg")
                    binding.btnFinishWorkout.isEnabled = true
                    Toast.makeText(this@WorkoutFinishActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JournalApiResponse<JournalDetailDto>>, t: Throwable) {
                Log.e("JaehoonLog", "통신 오류: ${t.message}")
                binding.btnFinishWorkout.isEnabled = true
            }
        })
    }
    private fun setupBodyParts() {
        bodyPartAdapter = BodyPartAdapter(mutableListOf()) { part ->
            binding.tvPainTagContent.text = part.name
            binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF"))
        }

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = bodyPartAdapter
        }

        binding.btnFront.setOnClickListener {
            currentDirection = "FRONT"
            updateDirectionTabUI(isFront = true)
            updateBodyPartList()
        }

        binding.btnBack.setOnClickListener {
            currentDirection = "BACK"
            updateDirectionTabUI(isFront = false)
            updateBodyPartList()
        }

        binding.chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            currentCategory = when (checkedId) {
                R.id.chip_head -> "머리/목"
                R.id.chip_upper -> "상체"
                R.id.chip_arm -> "팔/손"
                R.id.chip_lower -> "하체"
                R.id.chip_foot -> "발"
                else -> "머리/목"
            }
            updateBodyPartList()
        }
        updateBodyPartList()
    }

    private fun updateDirectionTabUI(isFront: Boolean) {
        if (isFront) {
            binding.btnFront.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnFront.setTextColor(Color.parseColor("#3A8DFF"))
            binding.btnBack.setBackgroundResource(android.R.color.transparent)
            binding.btnBack.setTextColor(Color.parseColor("#94A3B8"))
        } else {
            binding.btnBack.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnBack.setTextColor(Color.parseColor("#3A8DFF"))
            binding.btnFront.setBackgroundResource(android.R.color.transparent)
            binding.btnFront.setTextColor(Color.parseColor("#94A3B8"))
        }
    }

    private fun updateBodyPartList() {
        val key = "${currentDirection}_${currentCategory}"
        val names = bodyPartMap[key] ?: emptyList()
        val items = names.map { BodyPart(it) }
        bodyPartAdapter.updateItems(items)
    }
}