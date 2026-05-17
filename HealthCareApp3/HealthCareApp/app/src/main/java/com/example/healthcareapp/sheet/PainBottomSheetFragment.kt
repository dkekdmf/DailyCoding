package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.healthcareapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PainBottomSheetFragment(val onComplete: (String) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pain_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLeft = view.findViewById<TextView>(R.id.btn_left)
        val btnRight = view.findViewById<TextView>(R.id.btn_right)
        val btnComplete = view.findViewById<Button>(R.id.btn_complete)
        val btnClose = view.findViewById<ImageView>(R.id.btn_close)

        // 초기 선택 상태 (좌측 기본)
        btnLeft.isSelected = true

        btnLeft.setOnClickListener {
            btnLeft.isSelected = true
            btnRight.isSelected = false
        }

        btnRight.setOnClickListener {
            btnLeft.isSelected = false
            btnRight.isSelected = true
        }

        btnClose.setOnClickListener { dismiss() }

        btnComplete.setOnClickListener {
            // 입력값 처리 후 닫기
            onComplete("통증 기록 완료")
            dismiss()
        }
    }

    // 바텀시트 배경을 투명하게 해서 둥근 모서리가 보이게 설정
    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme
}