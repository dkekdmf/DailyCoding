package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.FolderItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FolderOptionSheet(
    val folder: FolderItem,       // 폴더 객체 데이터
    val onLinkClick: () -> Unit,  // 링크 생성 클릭 시 실행
    val onEditClick: () -> Unit,  // 이름 수정 클릭 시 실행
    val onExitClick: () -> Unit   // 폴더 나가기 클릭 시 실행
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 배경을 투명하게 하고 둥근 모서리를 적용
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bottomsheet1, container, false)


        val tvTitle = view.findViewById<TextView>(R.id.tv_sheet_title)      // 시트 상단 폴더명
        val btnClose = view.findViewById<View>(R.id.btn_close_sheet)        // 닫기 버튼
        val btnCreateLink = view.findViewById<View>(R.id.btn_create_link)   // 링크 생성 메뉴
        val btnEditName = view.findViewById<View>(R.id.btn_edit_folder_name)// 이름 수정 메뉴
        val btnExit = view.findViewById<View>(R.id.btn_exit_folder)         // 폴더 나가기 메뉴

        //현재 선택된 폴더의 이름을 타이틀에 표시
        tvTitle.text = folder.name
        val btnShareLink: View = view.findViewById(R.id.btn_create_link)
        //이미 공유 중인 폴더는 '링크 생성' 메뉴를 숨김 처리
        btnCreateLink.visibility = if (folder.isShared) View.GONE else View.VISIBLE



        // 닫기 버튼 클릭 시 시트 종료
        btnClose.setOnClickListener { dismiss() }

        // 링크 생성/공유 메뉴 클릭 시
        btnShareLink.setOnClickListener {
            onLinkClick() // 부모에게 알림
            dismiss()     // 메뉴창 닫기

        }

        // [이름 수정] 메뉴 클릭 시
        btnEditName.setOnClickListener {
            onEditClick() // 부모에게 알림
            dismiss()     // 메뉴창 닫기
        }

        // [폴더 나가기] 메뉴 클릭 시
        btnExit.setOnClickListener {
            onExitClick() // 부모에게 알림
            dismiss()     // 메뉴창 닫기
        }

        return view
    }



}