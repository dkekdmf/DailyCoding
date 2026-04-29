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


class FolderEditSheet(
    val currentName: String,             // 현재 폴더 이름
    val onEditConfirm: (String) -> Unit  // 수정 완료 시 실행할 콜백 함수
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 다이얼로그 배경을 투명하게 설정
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // folder_edit_sheet 레이아웃 파일을 연결
        return inflater.inflate(R.layout.folder_edit_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val etName = view.findViewById<EditText>(R.id.et_folder_name)       // 이름 입력란
        val tvCount = view.findViewById<TextView>(R.id.tv_char_count)       // 글자 수 표시 (0/15)
        val tvStatus = view.findViewById<TextView>(R.id.tv_status_message)  // 상태 메시지
        val btnSubmit = view.findViewById<Button>(R.id.btn_submit_edit)     // 변경하기 버튼
        val btnClear = view.findViewById<View>(R.id.btn_clear_text)         // 텍스트 전체 삭제(X) 버튼
        val btnClose = view.findViewById<View>(R.id.btn_close_edit)         // 닫기 버튼
        val btnBack = view.findViewById<View>(R.id.btn_back_edit)           // 뒤로가기 버튼

        // 초기 데이터 설정
        etName.setText(currentName)               // 기존 이름을 미리 입력창에 세팅
        etName.setSelection(etName.length())      // 커서를 텍스트 맨 뒤로 보냄
        tvCount.text = "${etName.length()}/15"    // 초기 글자 수 표시

        // 단순 닫기 리스너 설정
        btnClose.setOnClickListener { dismiss() }
        btnBack.setOnClickListener { dismiss() }

        // 텍스트 전체 삭제(X) 버튼 클릭 시 입력란 비우기
        btnClear.setOnClickListener { etName.setText("") }

        // 실시간 글자 수 체크 및 버튼 활성화 로직 (TextWatcher)
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCount.text = "$length/15" // 글자 수 업데이트

                // 글자가 있을 때만 'X' 버튼을 보여줌
                btnClear.visibility = if (length > 0) View.VISIBLE else View.GONE

                // 케이스별 UI 처리
                if (length > 15) {
                    // 글자 수 초과 시: 에러 메시지 빨간색
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
                    // 글자가 하나도 없을 때: 버튼 비활성화
                    btnSubmit.isEnabled = false
                    btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 변경하기 버튼 클릭 시
        btnSubmit.setOnClickListener {
            onEditConfirm(etName.text.toString()) // 입력된 이름을 콜백으로 전달
            dismiss() // 창 닫기
        }
    }

    // 다이얼로그의 크기를 설정
    override fun onStart() {
        super.onStart()
        // 화면 너비의 90% 정도 크기로 다이얼로그 크기를 강제 지정
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}