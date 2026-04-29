package com.example.healthcareapp

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    // 탭 레이아웃들을 멤버 변수로 선언
    private lateinit var tabFolder: LinearLayout
    private lateinit var tabJournal: LinearLayout
    private lateinit var tabMy: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navhome)

        // 1. 뷰 초기화
        tabFolder = findViewById(R.id.tab_folder)
        tabJournal = findViewById(R.id.tab_journal)
        tabMy = findViewById(R.id.tab_my)

        // 2. 앱 처음 실행 시 기본 화면 설정 (저장된 상태가 없을 때만)
        if (savedInstanceState == null) {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        // 3. 각 탭 클릭 리스너 설정
        tabFolder.setOnClickListener {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        tabJournal.setOnClickListener {
            replaceFragment(DiaryMainFragment())
            updateTabUI(tabJournal)
        }

        tabMy.setOnClickListener {
            // 마이페이지 프래그먼트가 있다면 여기에 replaceFragment(MyPageFragment()) 추가
            updateTabUI(tabMy)
        }
    }

    /**
     * [핵심 기능] 폴더 어댑터 등에서 호출하여 일지 탭으로 이동시키는 함수
     */
    fun moveToJournalTab(folderId: Long, folderName: String?) {
        // 일지 프래그먼트 생성 및 데이터 전달
        val diaryFragment = DiaryMainFragment().apply {
            arguments = Bundle().apply {
                putLong("FOLDER_ID", folderId)
                putString("FOLDER_NAME", folderName)
            }
        }

        replaceFragment(diaryFragment)
        updateTabUI(tabJournal) // 일지 탭 하이라이트 활성화
    }

    /**
     * 프래그먼트 교체 공통 로직 (애니메이션 포함)
     */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_container, fragment)
            .commit()
    }

    /**
     * 탭의 배경색과 아이콘 색상을 업데이트하는 함수
     */
    private fun updateTabUI(selectedTab: LinearLayout) {
        val tabs = listOf(tabFolder, tabJournal, tabMy)

        tabs.forEach { tab ->
            val isSelected = (tab == selectedTab)

            // 1. 배경 설정 (tab_selected 배경 적용 또는 제거)
            tab.setBackgroundResource(if (isSelected) R.drawable.tab_selected else 0)

            // 2. 아이콘(ImageView) 색상 변경 로직
            // 각 탭(LinearLayout)의 첫 번째 자식이 이미지뷰라는 가정하에 진행합니다.
            val icon = tab.getChildAt(0) as? ImageView
            icon?.let {
                if (isSelected) {
                    // 선택된 탭은 검정색 (#000000)
                    it.imageTintList = ColorStateList.valueOf(Color.BLACK)
                } else {
                    // 선택되지 않은 탭은 연한 회색 (#AAAAAA)
                    it.imageTintList = ColorStateList.valueOf(Color.parseColor("#AAAAAA"))
                }
            }
        }
    }
}