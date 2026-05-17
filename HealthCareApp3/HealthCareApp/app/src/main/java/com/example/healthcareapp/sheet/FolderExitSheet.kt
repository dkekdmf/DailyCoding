package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.healthcareapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class FolderExitSheet(
    val folderName: String,
    val onExitConfirm: () -> Unit // 나가기를 눌렀을 때 실행할 동작
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 배경을 투명하게,둥근모서리
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.folder_exit_sheet, container, false)

        // 폴더 나가기 문구

        view.findViewById<TextView>(R.id.tv_exit_title).text = "'$folderName' 폴더에서 나가시겠어요?"

        // 나가기 버튼 클릭리스너
        view.findViewById<Button>(R.id.btn_real_exit).setOnClickListener {
            onExitConfirm() // 부모 액티비티(FolderActivity)에 정의된 삭제/나가기 로직 실행
            dismiss()       // 바텀시트 닫기
        }

        // 3. 유지하기, 버튼클릭리스너
        view.findViewById<Button>(R.id.btn_stay).setOnClickListener {
            dismiss() // 아무 동작 없이 시트만 닫음
        }

        return view
    }
}