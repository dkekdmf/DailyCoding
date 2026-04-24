package com.example.healthcareapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.DialogFragment

class FolderAddCompleteSheet : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 배경을 투명하게 설정 (XML의 둥근 모서리 반영을 위해 필수)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.add_complete_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 확인 버튼 클릭 시 다이얼로그 닫기
        view.findViewById<Button>(R.id.btn_submit_add)?.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            // 1. 고정 수치(339dp x 271dp)를 픽셀 단위로 변환
            val widthPx = (339 * resources.displayMetrics.density).toInt()
            val heightPx = (289 * resources.displayMetrics.density).toInt()

            // 2. 윈도우 크기를 강제로 고정
            setLayout(widthPx, heightPx)

            // 3. 중앙 정렬 및 불필요한 시스템 여백 제거
            val params = attributes
            params.gravity = Gravity.CENTER
            attributes = params
        }
    }
}