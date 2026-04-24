package com.example.healthcareapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class FolderEditSheet(
    val currentName: String,
    val onEditConfirm: (String) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 배경을 투명하게 설정하여 XML의 둥근 모서리가 보이게 합니다.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 정해준 레이아웃 파일 하나만 인플레이트합니다. (이름 확인: folder_edit_dialog)
        return inflater.inflate(R.layout.folder_edit_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.et_folder_name)
        val tvCount = view.findViewById<TextView>(R.id.tv_char_count)
        val tvStatus = view.findViewById<TextView>(R.id.tv_status_message)
        val btnSubmit = view.findViewById<Button>(R.id.btn_submit_edit)
        val btnClear = view.findViewById<View>(R.id.btn_clear_text)
        val btnClose = view.findViewById<View>(R.id.btn_close_edit)
        val btnBack = view.findViewById<View>(R.id.btn_back_edit)

        // 초기 데이터 세팅
        etName.setText(currentName)
        etName.setSelection(etName.length())
        tvCount.text = "${etName.length()}/15" // 시안 기준 15자 혹은 18자 통일

        // 닫기 및 뒤로가기 버튼
        btnClose.setOnClickListener { dismiss() }
        btnBack.setOnClickListener { dismiss() }

        // 텍스트 전체 삭제 버튼
        btnClear.setOnClickListener { etName.setText("") }

        // 실시간 글자 수 감시
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCount.text = "$length/15"
                btnClear.visibility = if (length > 0) View.VISIBLE else View.GONE

                // 15자 초과 시 에러 처리 (시안에 따라 숫자 조정 가능)
                if (length > 15) {
                    tvStatus.text = "최대 15자 이내로 입력 가능합니다"
                    tvStatus.setTextColor(Color.parseColor("#FF6969"))
                    btnSubmit.isEnabled = false
                    btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
                } else if (length > 0) {
                    tvStatus.text = "한글, 영어, 숫자 혼용하여 최대 15자 이내로 입력"
                    tvStatus.setTextColor(Color.parseColor("#A8AFB9"))
                    btnSubmit.isEnabled = true
                    btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2D3A4B"))
                } else {
                    btnSubmit.isEnabled = false
                    btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 변경하기 버튼 클릭
        btnSubmit.setOnClickListener {
            onEditConfirm(etName.text.toString())
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그의 너비를 화면의 90% 정도로 설정하여 중앙에 예쁘게 배치
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}