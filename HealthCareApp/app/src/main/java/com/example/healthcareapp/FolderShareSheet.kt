package com.example.healthcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FolderShareSheet(
    val folderName: String,     // 공유할 폴더의 이름
    val onCopyClick: () -> Unit // 링크 복사 버튼 클릭 시 실행할 동작
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 배경 투명도 및 라운드 처리를 위해 커스텀 테마 스타일 적용
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.folder_share_sheet, container, false)

        // 타이틀 설정
        view.findViewById<TextView>(R.id.tv_share_title).text = "'$folderName' 폴더를 공유하시겠어요?"

        // 링크 복사 버튼 클릭 리스너 설정
        view.findViewById<Button>(R.id.btn_copy_link).setOnClickListener {
            // 부모 액티비티(FolderActivity)에서 전달받은 링크 복사 및 상태 변경 로직 실행
            onCopyClick()

            dismiss()
        }

        return view
    }
}