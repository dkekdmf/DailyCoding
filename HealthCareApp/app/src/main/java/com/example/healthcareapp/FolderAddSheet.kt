package com.example.healthcareapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

// 1. 중앙 배치를 위해 DialogFragment를 상속받습니다.
class FolderAddConfirmSheet(
    val onConfirm: () -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 2. 배경을 투명하게 설정하여 XML의 둥근 모서리(bottom_background2)가 보이게 합니다.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return inflater.inflate(R.layout.add_confrim_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 버튼 (X 아이콘)
        view.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            dismiss()
        }

        // 추가하기 버튼
        view.findViewById<Button>(R.id.btn_submit_add).setOnClickListener {
            onConfirm() // 부모 프래그먼트나 액티비티로 콜백 전달
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // 3. 다이얼로그의 가로 너비를 화면에 맞게 조절 (중앙 배치 핵심)
        val dialog = dialog
        if (dialog != null) {
            // dp를 pixel로 변환하는 공식 (339dp, 271dp 고정)
            val widthPx = (339 * resources.displayMetrics.density).toInt() // 너비 높이 고정
            val heightPx = (271 * resources.displayMetrics.density).toInt()

            dialog.window?.apply {
                // 시스템이 강제로 넣는 기본 패딩(여백)을 제거해야 고정 수치가 정확히 먹힙니다.
                setLayout(widthPx, heightPx)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                // 다이얼로그의 배경(Dim) 뒤로 그림자가 잘리지 않게 설정
                val params = attributes
                params.gravity = android.view.Gravity.CENTER
                attributes = params
            }
        }
    }
}