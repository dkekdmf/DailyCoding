package com.example.healthcareapp.sheet

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.healthcareapp.R


class FolderAddCompleteSheet : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 다이얼로그의 기본 배경을 투명하게 설정함.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // add_complete_sheet.xml 파일을 불러와서 화면에 붙임
        return inflater.inflate(R.layout.add_complete_sheet, container, false)
    }

    // 뷰가 생성된 직후 클릭 리스너 등을 설정
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.btn_submit_add)?.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }


    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            // 화면 밀도(density)를 가져와서 dp 단위를 px 단위로 변환
            // 해상도가 달라도 동일한 크기로 보이게끔 계산함
            val widthPx = (339 * resources.displayMetrics.density).toInt()  // 가로 339dp를 픽셀로 변환
            val heightPx = (289 * resources.displayMetrics.density).toInt() // 세로 289dp를 픽셀로 변환

            // 계산된 픽셀 크기로 다이얼로그 창의 크기를 강제 설정
            setLayout(widthPx, heightPx)

            // 다이얼로그를 화면의 정중앙에 배치
            val params = attributes
            params.gravity = Gravity.CENTER
            attributes = params
        }
    }
}