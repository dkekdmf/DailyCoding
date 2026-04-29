package com.example.healthcareapp

import android.content.Context
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

class AddExerciseActivity : AppCompatActivity() {
    private lateinit var binding: AddExerciseBinding
    private lateinit var adapter: ExerciseAddAdapter
    private var allExercises: List<ExerciseItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        allExercises = loadExercisesFromAssets(this)

        // 1. 리사이클러뷰 설정
        setupRecyclerView()

        // 2. 카테고리 칩 스타일 초기화 (보라색 제거)
        initChipStyles()

        // 3. 필터링 리스너
        setupCategoryFilter()

        // 초기 데이터 로드
        if (allExercises.isNotEmpty()) {
            adapter.updateList(allExercises)
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun initChipStyles() {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf()
        )
        val colors = intArrayOf(
            Color.parseColor("#4A5768"),
            Color.parseColor("#4A5768"),
            Color.parseColor("#F2F4F6")
        )
        val backgroundStateList = ColorStateList(states, colors)

        val textColors = intArrayOf(
            Color.WHITE,
            Color.WHITE,
            Color.parseColor("#8E949A")
        )
        val textStateList = ColorStateList(states, textColors)

        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i) as? Chip
            chip?.apply {
                chipBackgroundColor = backgroundStateList
                setTextColor(textStateList)
                isCheckedIconVisible = false
                rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
                chipStrokeWidth = 0f
            }
        }
    }

    private fun setupRecyclerView() {
        // 어댑터에서 선택된 리스트 전체를 받아오도록 콜백 수정
        adapter = ExerciseAddAdapter(allExercises) { selectedList ->
            // 하단 버튼 업데이트
            updateBottomButton(selectedList.size)
            // 상단 선택된 운동 태그 업데이트
            updateSelectedExerciseTags(selectedList)
        }
        binding.rvExerciseList.layoutManager = LinearLayoutManager(this)
        binding.rvExerciseList.adapter = adapter
    }

    // 하단 버튼 상태 업데이트

    private fun updateBottomButton(count: Int) {
        binding.btnSelectExercise.apply {
            // 1. 폰트 설정 (파일명이 pretendard_bold.otf 일 경우)
            val tf = ResourcesCompat.getFont(this@AddExerciseActivity, R.font.pretendard)
            typeface = tf

            if (count > 0) {
                // 운동이 1개라도 선택된 경우 (네이비 상태)
                text = "+ ${count}개의 운동 추가하기"
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4A5768")) // 네이비
                isEnabled = true // 버튼 활성화
            } else {
                // 다시 0개가 된 경우 (회색 상태)
                text = "+ 0개의 운동 추가하기"
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B1B8C0")) // 회색
                isEnabled = false // 버튼 비활성화
            }

            // 텍스트 색상은 항상 흰색 유지
            setTextColor(Color.WHITE)
        }
    }

    // 상단에 선택된 운동 칩(X버튼 포함) 동적 생성
    private fun updateSelectedExerciseTags(selectedList: List<ExerciseItem>) {
        binding.chipGroupSelectedExercises.removeAllViews()

        selectedList.forEach { item ->
            val chip = Chip(this).apply {
                text = item.name
                isCloseIconVisible = true // X 아이콘 활성화
                closeIconSize = 35f
                setCloseIconTintResource(android.R.color.darker_gray)

                // 디자인 반영: 흰 배경에 회색 테두리
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#D1D8E0"))
                chipStrokeWidth = 2f
                setTextColor(Color.parseColor("#8E949A"))
                textSize = 12f

                // X 아이콘 클릭 시 해당 운동 선택 해제
                setOnCloseIconClickListener {
                    adapter.removeSelection(item.id)
                }
            }
            binding.chipGroupSelectedExercises.addView(chip)
        }
    }

    private fun setupCategoryFilter() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val categoryName = if (checkedIds.isEmpty()) "전체"
            else group.findViewById<Chip>(checkedIds.first()).text.toString()

            filterExercises(categoryName)
        }
    }

    private fun filterExercises(category: String) {
        val filteredList = if (category == "전체") {
            allExercises
        } else {
            allExercises.filter { item ->
                val part = item.bodyPart.trim()
                val target = item.target.trim()
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
        adapter.updateList(filteredList)
        binding.rvExerciseList.scrollToPosition(0)
    }

    private fun loadExercisesFromAssets(context: Context): List<ExerciseItem> {
        return try {
            val jsonString = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<ExerciseItem>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (ioException: IOException) {
            emptyList()
        }
    }
}