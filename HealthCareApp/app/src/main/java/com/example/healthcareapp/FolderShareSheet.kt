package com.example.healthcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FolderShareSheet(
    val folderName: String,
    val onCopyClick: () -> Unit
) : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // onCreateView가 호출되기 전에 스타일이 적용되어야 배경 투명이 먹힙니다.
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.folder_share_sheet, container, false)

        view.findViewById<TextView>(R.id.tv_share_title).text = "'$folderName' 폴더를 공유하시겠어요?"

        view.findViewById<Button>(R.id.btn_copy_link).setOnClickListener {
            onCopyClick()
            dismiss()
        }

        return view
    }
}