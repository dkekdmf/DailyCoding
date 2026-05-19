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
        rgPainStage.check(R.id.rg_pain_level) // XML의 실제 단계 ID에 맞게 수정 필요

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

        // 🎯 [수정된 단계 선택 로직] 각 라디오 버튼 ID에 매핑
        rgPainStage.setOnCheckedChangeListener { _, checkedId ->
            selectedStage = when (checkedId) {
                R.id.rg_pain_level -> "1"
                R.id.rg_pain_level -> "2"
                R.id.rg_pain_level -> "3"
                R.id.rg_pain_level -> "4"
                R.id.rg_pain_level -> "5"
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