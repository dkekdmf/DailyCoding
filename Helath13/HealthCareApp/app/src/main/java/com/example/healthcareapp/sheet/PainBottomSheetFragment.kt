package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.healthcareapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PainBottomSheetFragment(
    private val partName: String,
    private val onComplete: (direction: String, stage: String, memo: String) -> Unit
) : BottomSheetDialogFragment() {

    private var selectedDirection = "좌"
    private var selectedStage = "3" // 기본값

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.pain_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLeft = view.findViewById<TextView>(R.id.btn_left)
        val btnRight = view.findViewById<TextView>(R.id.btn_right)
        val btnComplete = view.findViewById<Button>(R.id.btn_complete)
        val btnClose = view.findViewById<ImageView>(R.id.btn_close)
        val rgPainStage = view.findViewById<RadioGroup>(R.id.rg_pain_level)
        val etPainMemo = view.findViewById<EditText>(R.id.et_pain_memo)

        // 초기 상태 설정
        btnLeft.isSelected = true

        btnLeft.setOnClickListener {
            btnLeft.isSelected = true
            btnRight.isSelected = false
            selectedDirection = "좌"
        }

        btnRight.setOnClickListener {
            btnLeft.isSelected = false
            btnRight.isSelected = true
            selectedDirection = "우"
        }

        // 🎯 [수술 완료] 라디오 그룹 내 개별 버튼 ID들을 정확하게 타겟팅 분기 처리
        rgPainStage.setOnCheckedChangeListener { _, checkedId ->
            selectedStage = when (checkedId) {
                R.id.rb_level1 -> "1"
                R.id.rb_level2 -> "2"
                R.id.rb_level3 -> "3"
                R.id.rb_level4 -> "4"
                R.id.rb_level5 -> "5"
                else -> "3"
            }
        }

        btnClose.setOnClickListener { dismiss() }

        btnComplete.setOnClickListener {
            val userMemo = etPainMemo?.text?.toString() ?: ""
            onComplete(selectedDirection, selectedStage, userMemo)
            dismiss()
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme
}