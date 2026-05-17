package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.healthcareapp.data.ExerciseItem
import com.example.healthcareapp.databinding.AddExerciseBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

/**
 * 운동 추가 화면: 전체 운동 목록에서 부위별 필터링을 통해 운동을 선택함
 */
class AddExerciseActivity : AppCompatActivity() {
    private lateinit var binding: AddExerciseBinding
    private lateinit var adapter: ExerciseAddAdapter
    private var allExercises: List<ExerciseItem> = listOf() // JSON에서 로드된 전체 운동 데이터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. assets/exercises.json 파일에서 운동 리스트를 로드함
        allExercises = loadExercisesFromAssets(this)

        setupRecyclerView()      // 리사이클러뷰 및 선택 리스너 설정
        initChipStyles()         // 상단 카테고리 칩 디자인(색상, 상태) 초기화
        setupCategoryFilter()    // 카테고리 칩 클릭 시 필터링 로직 연결

        // [운동 추가하기] 버튼 클릭: 선택된 항목들을 Array로 만들어 결과 반환
        binding.btnSelectExercise.setOnClickListener {
            val selectedExercises = adapter.getSelectedItems()

            if (selectedExercises.isNotEmpty()) {
                val selectedNames = selectedExercises.map { it.name }.toTypedArray()
                val intent = Intent().apply {
                    putExtra("exercise_names", selectedNames)
                }
                setResult(Activity.RESULT_OK, intent) // WorkoutExerciseActivity로 데이터 전달
                finish()
            }
        }

        // 초기 화면 진입 시 전체 리스트 표시
        if (allExercises.isNotEmpty()) {
            adapter.updateList(allExercises)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    /**
     * 상단 카테고리 칩(전체, 가슴, 등 등)의 선택 상태별 색상을 동적으로 설정
     */
    private fun initChipStyles() {
        // 칩의 상태(선택됨, 눌림, 기본)에 따른 배경색 설정
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf()
        )
        val colors = intArrayOf(
            Color.parseColor("#4A5768"), // 선택 시 진한 회색
            Color.parseColor("#4A5768"),
            Color.parseColor("#F2F4F6")  // 기본 연한 회색
        )
        val backgroundStateList = ColorStateList(states, colors)

        // 텍스트 색상 설정 (선택 시 흰색, 기본은 회색)
        val textColors = intArrayOf(
            Color.WHITE,
            Color.WHITE,
            Color.parseColor("#8E949A")
        )
        val textStateList = ColorStateList(states, textColors)

        // ChipGroup 내부의 모든 칩에 스타일 일괄 적용
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i) as? Chip
            chip?.apply {
                chipBackgroundColor = backgroundStateList
                setTextColor(textStateList)
                isCheckedIconVisible = false // 선택 시 체크 표시 숨김
                rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
                chipStrokeWidth = 0f
            }
        }
    }

    /**
     * 운동 목록 리사이클러뷰 초기화 및 선택 변경 시 UI 업데이트 콜백 설정
     */
    private fun setupRecyclerView() {
        adapter = ExerciseAddAdapter(allExercises) { selectedList ->
            // 항목 선택/해제 시마다 하단 버튼의 숫자와 상단 선택 태그 리스트를 갱신
            updateBottomButton(selectedList.size)
            updateSelectedExerciseTags(selectedList)
        }
        binding.rvExerciseList.layoutManager = LinearLayoutManager(this)
        binding.rvExerciseList.adapter = adapter
    }

    /**
     * 선택된 운동 개수에 따라 하단 추가 버튼의 텍스트와 활성화 상태 변경
     */
    private fun updateBottomButton(count: Int) {
        binding.btnSelectExercise.apply {
            val tf = ResourcesCompat.getFont(this@AddExerciseActivity, R.font.pretendard)
            typeface = tf

            if (count > 0) {
                text = "+ ${count}개의 운동 추가하기"
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4A5768"))
                isEnabled = true
            } else {
                text = "+ 0개의 운동 추가하기"
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B1B8C0"))
                isEnabled = false
            }
            setTextColor(Color.WHITE)
        }
    }

    /**
     * 선택된 운동들을 화면 중앙에 작은 Chip 형태로 나열
     */
    private fun updateSelectedExerciseTags(selectedList: List<ExerciseItem>) {
        binding.chipGroupSelectedExercises.removeAllViews() // 기존 태그 비우기

        selectedList.forEach { item ->
            val chip = Chip(this).apply {
                text = item.name
                isCloseIconVisible = true // 삭제(X) 아이콘 활성화
                closeIconSize = 35f
                setCloseIconTintResource(android.R.color.darker_gray)

                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#D1D8E0"))
                chipStrokeWidth = 2f
                setTextColor(Color.parseColor("#8E949A"))
                textSize = 12f

                // X 아이콘 클릭 시 리사이클러뷰 어댑터에서도 해당 항목 선택 해제
                setOnCloseIconClickListener {
                    adapter.removeSelection(item.id)
                }
            }
            binding.chipGroupSelectedExercises.addView(chip)
        }
    }

    /**
     * 카테고리 칩 클릭 시 필터링 로직 실행
     */
    private fun setupCategoryFilter() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val categoryName = if (checkedIds.isEmpty()) "전체"
            else group.findViewById<Chip>(checkedIds.first()).text.toString()

            filterExercises(categoryName)
        }
    }

    /**
     * 선택된 카테고리 문자열에 따라 데이터 리스트 필터링
     */
    private fun filterExercises(category: String) {
        val filteredList = if (category == "전체") {
            allExercises
        } else {
            allExercises.filter { item ->
                val part = item.bodyPart.trim()
                val target = item.target.trim()
                // JSON의 데이터 구조(부위, 타겟 근육)에 맞춰 키워드 매칭
                when (category) {
                    "유산소" -> part == "유산소" || part == "심장" || target == "심혈관"
                    "하체" -> part == "하체" || part == "허리" || part.contains("다리") || part == "허벅지"
                    "가슴" -> part == "가슴"
                    "등" -> part == "등"
                    "어깨" -> part == "어깨"
                    "팔" -> part == "팔" || part.contains("이두") || part.contains("삼두")
                    else -> false
                }
            }
        }
        adapter.updateList(filteredList) // 필터링된 결과로 리스트 갱신
        binding.rvExerciseList.scrollToPosition(0) // 목록 상단으로 이동
    }

    /**
     * assets 폴더의 exercises.json 파일을 읽어와서 객체 리스트로 변환
     */
    private fun loadExercisesFromAssets(context: Context): List<ExerciseItem> {
        return try {
            val jsonString = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<ExerciseItem>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            emptyList()
        }
    }
}