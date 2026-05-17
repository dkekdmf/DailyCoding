package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.StatusQuestionAdapter
import com.example.healthcareapp.adapter.BodyPartAdapter
import com.example.healthcareapp.data.JournalSummaryDto
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.StatusQuestionBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * 운동 종료 후 요약 정보 표시 및 컨디션/통증 기록 화면
 */
class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: StatusQuestionBinding
    private lateinit var questionAdapter: StatusQuestionAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter
    private var savedExerciseList: ArrayList<com.example.healthcareapp.data.ExerciseRecord> = arrayListOf()
    private lateinit var questionList: List<StatusQuestion>

    private var receivedWorkoutType: String = "개인운동"
    private var folderId: Long = -1L
    private var savedTotalTime: String = "00:00:00"
    private var savedStartTime: String = "00:00"
    private var savedEndTime: String = "00:00"
    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private val bodyPartMap = mapOf(
        "FRONT_머리/목" to listOf("머리", "이마", "얼굴", "목"),
        "FRONT_상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
        "FRONT_팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
        "FRONT_하체" to listOf("고관절", "사타구니", "허벅지", "무릎", "정강이"),
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

        folderId = intent.getLongExtra("FOLDER_ID", -1L)
        receivedWorkoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "개인운동"

        android.util.Log.d("JaehoonTest", "WorkoutFinishActivity onCreate - 폴더ID: $folderId, 타입: $receivedWorkoutType")

        setupTimeInfo()
        setupStatusQuestions()
        setupBodyParts()
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.btnFinishWorkout.setOnClickListener {
            val memo = binding.etFeedbackMemo.text.toString()
            val index = (System.currentTimeMillis() / 1000 % emojiList.size).toInt()
            val selectedEmojiResId = emojiList[index]

            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }

            // 🎯 [순정 보존] 진짜 운동 세트 데이터 리스트를 DTO에 포함해 동기화 빌드합니다.
            val newJournal = JournalSummaryDto(
                folderId = folderId,
                workoutType = receivedWorkoutType,
                createdAt = dateTimeFormat.format(Date()),
                totalTime = savedTotalTime,
                memo = memo,
                condition = "GOOD",
                emojiResId = selectedEmojiResId,
                startTime = savedStartTime,
                endTime = savedEndTime,
                exerciseList = savedExerciseList
            )

            try {
                val prefs = getSharedPreferences("HealthDiaryPrefs", Context.MODE_PRIVATE)
                val journals = DiaryPrefsManager.getJournals(this, folderId).toMutableList()
                journals.add(newJournal)

                val json = com.google.gson.Gson().toJson(journals)
                prefs.edit().putString("diary_list_$folderId", json).commit() // 🏁 동기 저장 고정
                Log.d("JaehoonSync", "💾 운동 세트 데이터까지 포함하여 물리 디스크 동기화(commit) 완료!")
            } catch (e: Exception) {
                Log.e("JaehoonSync", "동기 저장 중 에러: ${e.message}")
            }

            val resultIntent = Intent().apply {
                putExtra("EMOJI_RES_ID", selectedEmojiResId)
                putExtra("WORKOUT_TYPE", receivedWorkoutType)
                putExtra("TOTAL_TIME", savedTotalTime)
                putExtra("START_TIME", savedStartTime)
                putExtra("END_TIME", savedEndTime)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupTimeInfo() {
        savedTotalTime = intent.getStringExtra("TOTAL_TIME") ?: "00:00:00"
        savedStartTime = intent.getStringExtra("START_TIME") ?: "00:00"
        savedEndTime = intent.getStringExtra("END_TIME") ?: "00:00"

        val rawList = intent.getSerializableExtra("EXERCISE_LIST") as? ArrayList<com.example.healthcareapp.data.ExerciseRecord>
        if (rawList != null) {
            savedExerciseList = rawList
            android.util.Log.d("JaehoonSync", "📥 FinishActivity 세트 데이터 수신 성공: ${savedExerciseList.size}개")
        }

        binding.tvTotalTime.text = savedTotalTime
        binding.tvStartTime.text = "시작\n$savedStartTime"
        binding.tvEndTime.text = "종료\n$savedEndTime"
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

    private fun setupBodyParts() {
        bodyPartAdapter = BodyPartAdapter(mutableListOf()) { part ->
            binding.tvPainTagContent.text = part.name
            binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF"))
        }

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = bodyPartAdapter
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