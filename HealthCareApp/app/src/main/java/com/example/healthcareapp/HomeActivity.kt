package com.example.healthcareapp

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navhome)
// 하단 탭 버튼들 찾아오기
        val tabFolder = findViewById<LinearLayout>(R.id.tab_folder)
        val tabJournal = findViewById<LinearLayout>(R.id.tab_journal)
        val tabMy = findViewById<LinearLayout>(R.id.tab_my)

        // 1. 앱 실행 시 첫 화면 설정 (폴더 프래그먼트)
        if (savedInstanceState == null) {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder) // 폴더 탭 활성화 상태로 시작
        }

        // 2. 각 탭 클릭 이벤트 설정
        tabFolder.setOnClickListener {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        tabJournal.setOnClickListener {
            // JournalFragment는 나중에 만드실 때 연결하세요!
            // replaceFragment(JournalFragment())
            updateTabUI(tabJournal)
        }

        tabMy.setOnClickListener {
            // MyFragment도 나중에 만드실 때 연결!
            // replaceFragment(MyFragment())
            updateTabUI(tabMy)
        }
    }

    // 화면(프래그먼트)을 교체하는 함수
    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .commit()
    }

    // 선택된 탭의 배경색을 바꿔주는 함수 (하이라이트 효과)
    private fun updateTabUI(selectedTab: LinearLayout) {
        val tabs = listOf(
            findViewById<LinearLayout>(R.id.tab_folder),
            findViewById<LinearLayout>(R.id.tab_journal),
            findViewById<LinearLayout>(R.id.tab_my)
        )

        tabs.forEach { tab ->
            if (tab == selectedTab) {

                tab.setBackgroundResource(R.drawable.tab_selected)

            } else {

                tab.setBackgroundResource(0)
            }
        }
    }
}