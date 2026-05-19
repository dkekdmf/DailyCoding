package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import com.example.healthcareapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 🎯 [피그마 사양 완벽 동기화] 세부 부위별 통증 디테일(방향, 단계, 사유)을 상위 액티비티로 수송하는 바텀시트
 */
class PainBottomSheetFragment(
    private val partName: String,
    private val onComplete: (direction: String, stage: String, memo: String) -> Unit
) : BottomSheetDialogFragment() {

    // 피그마 UI 기준 기본 초기 세팅값 고정
    private var selectedDirection = "좌"
    private var selectedStage = "3" // 기본 3단계 활성화

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

        // 🎯 [피그마 인풋 컴포넌트 저격] 라디오그룹 및 에디트텍스트 바인딩
        val rgPainStage = view.findViewById<RadioGroup>(R.id.rg_pain_level)
        val etPainMemo = view.findViewById<EditText>(R.id.et_pain_memo) ?: view.findViewById(R.id.et_pain_memo)

        // 초기 선택 상태 (좌측 기본 고정)
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

        // 🎯 [라디오 버튼 변경 감지 리액터] 1~5단계 피킹 실시간 동기화
        rgPainStage?.setOnCheckedChangeListener { _, checkedId ->
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
            // 유저가 작성한 메모 텍스트 안전하게 확보
            val userMemo = etPainMemo?.text?.toString() ?: ""

            // 🎬 [데이터 파이프라인 가동] 조립된 3개의 필드를 WorkoutFinishActivity로 발송!
            onComplete(selectedDirection, selectedStage, userMemo)
            dismiss()
        }
    }

    // 바텀시트 배경을 투명하게 해서 둥근 모서리가 보이게 설정
    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme
}