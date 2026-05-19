package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.StatusQuestionAdapter
import com.example.healthcareapp.adapter.BodyPartAdapter
import com.example.healthcareapp.data.JournalSummaryDto
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.StatusQuestionBinding
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 운동 종료 후 요약 정보 표시 및 컨디션/통증 기록 화면 (순정 레이아웃 100% 밀착 연동본)
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

    private var selectedImageBase64: String? = null

    // 🎯 [실시간 통증 데이터 캐싱 공간] 피그마 구조 저격용 상태 관리 리스트
    private val painTagsList = mutableListOf<PainTagItem>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                selectedImageBase64 = uriToBase64(imageUri)
                binding.ivPreview.setImageURI(imageUri)
                binding.ivPreview.imageTintList = null
                Log.d("JaehoonLog", "📸 프리뷰 이미지 표출 완료 및 Base64 직렬화 성공!")
            }
        }
    }

    private val bodyPartMap = mapOf(
        "FRONT_머리/목" to listOf("머리", "이마", "얼굴", "목"),
        "FRONT_상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
        "FRONT_팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
        "FRONT_하체" to listOf("고관절", "사타구니", "생식기", "허벅지", "무릎", "정강이"),
        "FRONT_발" to listOf("발목", "발등", "발가락"),

        "BACK_머리/목" to listOf("경추 (목뼈 부위)"),
        "BACK_상체" to listOf("등", "어깨", "날개(견갑골)", "허리", "꼬리뼈"),
        "BACK_팔/손" to listOf("윗팔", "팔꿈치", "어랫팔", "손목", "손바닥", "손가락"),
        "BACK_하체" to listOf("엉덩이", "뒷허벅지", "오금", "종아리"),
        "BACK_발" to listOf("아킬레스건", "발바닥")
    )

    private var currentDirection = "FRONT" // FRONT(앞면), BACK(뒷면)
    private var currentCategory = "머리/목"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatusQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        folderId = intent.getLongExtra("FOLDER_ID", -1L)
        receivedWorkoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "개인운동"

        setupTimeInfo()
        setupStatusQuestions()
        setupBodyParts()
        initClickListeners()
        updatePainContainerVisibility() // 초기 통증 안내 바인딩 정비
        updateDirectionTabCountText()   // 상단 앞면/뒷면 배지 글자 초기화
    }

    private fun initClickListeners() {
        binding.btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnRemovePhoto.setOnClickListener {
            selectedImageBase64 = null
            binding.ivPreview.setImageDrawable(null)
            Log.d("JaehoonLog", "📸 첨부 인증샷 이미지 가뿐하게 제거")
        }

        // 🎯 [순정 X 단추 매싱] 기존 태그 컨테이너 자체 혹은 내부 클릭 시 기록 초기화 연동
        binding.layoutPainTagContainer.setOnClickListener {
            if (painTagsList.isNotEmpty()) {
                // 마지막 기록 지우고 새로고침
                painTagsList.removeAt(painTagsList.size - 1)
                updateDirectionTabCountText()
                updatePainContainerVisibility()
                Toast.makeText(this, "통증 기록이 정상적으로 지워졌습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFinishWorkout.setOnClickListener {
            val memo = binding.etFeedbackMemo.text.toString()
            val index = (System.currentTimeMillis() / 1000 % emojiList.size).toInt()
            val selectedEmojiResId = emojiList[index]
            val finalScores = questionAdapter.getCurrentScores() // 어댑터에서 점수 가져오기
            val finalPain = binding.tvPainTagContent.text.toString()
            val scoreStr = finalScores.joinToString(",") { score -> score.toInt().toString() }
            val formattedMemo = "운동 후 상태체크 01#[SCORE]$scoreStr#$memo"
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }
            val newJournal = JournalSummaryDto(
                id = System.currentTimeMillis(),
                folderId = folderId,
                workoutType = receivedWorkoutType,
                createdAt = dateTimeFormat.format(Date()),
                totalTime = savedTotalTime,
                memo = memo,
                condition = "GOOD",
                emojiResId = selectedEmojiResId,
                startTime = savedStartTime,
                endTime = savedEndTime,
                exerciseList = savedExerciseList,
                conditionScores = finalScores,
                conditionMemo = formattedMemo,
                painTag = "운동 후 상태체크 01#$finalPain",
                imageString = selectedImageBase64 ?: ""
            )

            try {
                val prefs = getSharedPreferences("HealthDiaryPrefs", Context.MODE_PRIVATE)
                val journals = DiaryPrefsManager.getJournals(this, folderId).toMutableList()
                journals.add(newJournal)

                val json = com.google.gson.Gson().toJson(journals)
                prefs.edit().putString("diary_list_$folderId", json).commit()
                Log.d("JaehoonSync", "💾 물리 디스크 동기화 완료!")
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
        }

        binding.tvTotalTime.text = savedTotalTime
        binding.tvStartTime.text = "시작\n$savedStartTime"
        binding.tvEndTime.text = "종료\n$savedEndTime"
    }

    private fun setupStatusQuestions() {
        questionList = listOf(
            StatusQuestion(1, "운동 후 평소와 다른 관절이나\n근육 통증이 있었나요?", "매우 심함", "통증 없음", 10,
                mapOf(
                    1 to "1 - 매우 심함", 2 to "2 - 통증이 많이 심함", 3 to "3 - 통증이 뚜렷함",
                    4 to "4 - 통증이 꽤 느껴짐", 5 to "5 - 통증이 분명함", 6 to "6 - 약간 신경 쓰임",
                    7 to "7 - 가벼운 불편감", 8 to "8 - 아주 약함", 9 to "9 - 거의 없음", 10 to "10 - 통증 없음"
                )),
            StatusQuestion(2, "오늘 운동 강도는 내 몸 상태에\n적절했나요?", "너무 약하거나 무리", "딱 맞았음", 8,
                mapOf(
                    1 to "1 - 너무 약하거나 무리", 2 to "2 - 많이 안 맞음", 3 to "3 - 안 맞는 편",
                    4 to "4 - 조금 아쉬움", 5 to "5 - 무난하지만 애매", 6 to "6 - 크게 무리 없음",
                    7 to "7 - 대체로 잘 맞음", 8 to "8 - 잘 맞음", 9 to "9 - 매우 잘 맞음", 10 to "10 - 딱 맞음"
                )),
            StatusQuestion(3, "운동 후 어지러움이나\n불편감이 있었나요?", "매우 심했음", "전혀 없었음", 10,
                mapOf(
                    1 to "1 - 매우 심했음", 2 to "2 - 많이 심했음", 3 to "3 - 심한 편이었음",
                    4 to "4 - 꽤 불편했음", 5 to "5 - 분명히 느껴졌음", 6 to "6 - 조금 있었음",
                    7 to "7 - 약하게 있었음", 8 to "8 - 아주 미세했음", 9 to "9 - 거의 없었음", 10 to "10 - 전혀 없었음"
                )),
            StatusQuestion(4, "운동 후 전반적인 기분 상태는\n어떤가요?", "매우 안 좋음", "최상 ", 7,
                mapOf(
                    1 to "1 - 매우 안 좋음", 2 to "2 - 많이 안 좋은 상태", 3 to "3 - 안 좋은 편",
                    4 to "4 - 다소 안 좋은 상태", 5 to "5 - 보통 이하", 6 to "6 - 무난한 상태",
                    7 to "7 - 괜찮은 편", 8 to "8 - 좋은 편", 9 to "9 - 매우 좋음", 10 to "10 - 최상"
                )),
            StatusQuestion(5, "오늘 계획한 운동 목표를\n달성했나요?", "거의 못 함", "계획보다 많이 더 함", 8,
                mapOf(
                    1 to "1 - 거의 못 함", 2 to "2 - 조금만 함", 3 to "3 - 일부만 함",
                    4 to "4 - 절반도 못 함", 5 to "5 - 절반 정도 함", 6 to "6 - 절반 넘게 함",
                    7 to "7 - 대부분 함", 8 to "8 - 계획한 만큼 함", 9 to "9 - 계획보다 조금 더 함", 10 to "10 - 계획보다 많이 더 함"
                ))
        )

        questionAdapter = StatusQuestionAdapter(questionList)
        binding.rvStatusQuestions.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = questionAdapter
        }
    }
    private fun setupBodyParts() {
        val rawStringList = bodyPartMap["${currentDirection}_${currentCategory}"] ?: emptyList()
        val detailList = rawStringList.map { BodyPart(it) }.toMutableList()

        bodyPartAdapter = BodyPartAdapter(detailList) { clickedPart ->
            val partString = clickedPart.name

            val bottomSheet = PainBottomSheetFragment(partString) { direction, stage, memo ->

                // 1. 기존에 쌓인 거 싹 치우고 가장 최근에 누른 딱 '1개'의 데이터만 캐싱합니다.
                painTagsList.clear()
                painTagsList.add(PainTagItem(
                    directionType = if (currentDirection == "FRONT") "앞면" else "뒷면",
                    direction = direction,
                    partName = partString,
                    stage = stage,
                    memo = memo
                ))

                // 2. 상단 배지 카운팅 동기화 (무조건 1 아니면 0이 되겠죠!)
                updateDirectionTabCountText()

                // 3. 🎬 하단 순정 레이아웃에 단 한 줄로 기분 좋게 갱신 처리!
                updatePainContainerVisibility()
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = bodyPartAdapter
        }

        // --- 하위 칩 그룹 및 앞/뒷면 탭 클릭 리스너 순정 로직 유지 ---
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


    // 🎯 [정렬 깨짐 완전 진압] 순정 XML 테두리와 패딩을 완벽하게 보존하면서 세로로 촘촘하게 누적합니다.
    // 🎯 [UI 파괴 방지 가드] 동적 addView를 완전히 걷어내고, XML에 정의된 기존 TextView 문구만 정갈하게 교체합니다!
    private fun updatePainContainerVisibility() {
        if (painTagsList.isNotEmpty()) {
            binding.layoutPainTagContainer.visibility = View.VISIBLE
            val latestTag = painTagsList.last()

            // ⭐ [재훈님 요청 띄어쓰기 규격] "좌: 팔꿈치 : 통증정도: 3단계"로 글자만 착 바꿉니다.
            binding.tvPainTagContent.text = "${latestTag.direction}: ${latestTag.partName} : 통증정도: ${latestTag.stage}단계"

            // 🎨 파란색 걷어내고 피그마 원본 다크 테마 컬러 적용
            binding.tvPainTagContent.setTextColor(Color.parseColor("#2D3A4B"))
        } else {
            // 초기 상태거나 제거 시 순정 가드 문구 출력
            binding.tvPainTagContent.text = "기록된 통증이 없습니다"
            binding.tvPainTagContent.setTextColor(Color.parseColor("#94A3B8"))
        }
    }
    // 앞면 1, 뒷면 0 상단 실시간 카운팅 동기화 탭 로직 유지
    private fun updateDirectionTabCountText() {
        val frontCount = painTagsList.count { it.directionType == "앞면" }
        val backCount = painTagsList.count { it.directionType == "뒷면" }

        binding.btnFront.text = "앞면 $frontCount"
        binding.btnBack.text = "뒷면 $backCount"
    }

    private fun updateDirectionTabUI(isFront: Boolean) {
        if (isFront) {
            binding.btnFront.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnFront.setTextColor(Color.parseColor("#000000"))
            binding.btnBack.setBackgroundResource(android.R.color.transparent)
            binding.btnBack.setTextColor(Color.parseColor("#94A3B8"))
        } else {
            binding.btnBack.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnBack.setTextColor(Color.parseColor("#000000"))
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

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("JaehoonLog", "이미지 변환 실패: ${e.message}")
            null
        }
    }

    data class PainTagItem(
        val directionType: String, // 앞면, 뒷면
        val direction: String,     // 좌, 우
        val partName: String,      // 이마, 팔꿈치 등
        val stage: String,         // 1~5
        val memo: String
    )
}