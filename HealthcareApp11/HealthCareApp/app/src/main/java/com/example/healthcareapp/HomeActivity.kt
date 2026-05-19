package com.example.healthcareapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.healthcareapp.fragment.DiaryMainFragment
import com.example.healthcareapp.fragment.DiaryListFragment
import com.example.healthcareapp.fragment.FolderMainFragment
import com.example.healthcareapp.fragment.MyPageFragment

/**
 * 앱의 메인 화면: 하단 네비게이션 탭 관리 및 프래그먼트 전환 컨트롤러
 */
class HomeActivity : AppCompatActivity() {

    // 하단 탭 레이아웃 변수 (LinearLayout 구조)
    private lateinit var tabFolder: LinearLayout
    private lateinit var tabJournal: LinearLayout
    private lateinit var tabMy: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navhome)

        // 1. 하단 탭 뷰 초기화
        tabFolder = findViewById(R.id.tab_folder)
        tabJournal = findViewById(R.id.tab_journal)
        tabMy = findViewById(R.id.tab_my)

        // 2. 초기 화면 설정: 처음 실행 시 '폴더' 화면을 띄움
        if (savedInstanceState == null) {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        // 3. 하단 탭 클릭 리스너: 각 탭 클릭 시 해당 프래그먼트로 교체 및 UI 업데이트
        tabFolder.setOnClickListener {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        tabJournal.setOnClickListener {
            replaceFragment(DiaryMainFragment())
            updateTabUI(tabJournal)
        }

        tabMy.setOnClickListener {
            replaceFragment(MyPageFragment())
            updateTabUI(tabMy)
        }
    }

    /**
     * [폴더 클릭 시 호출] FolderMainFragment 내의 어댑터에서 호출됨.
     * 선택된 폴더 정보를 가지고 '일지 메인(캘린더)' 화면으로 이동.
     */
    fun moveToJournalTab(folderId: Long, folderName: String, isSharedMode: Boolean) {
        val journalFragment = DiaryMainFragment().apply {
            arguments = Bundle().apply {
                putLong("FOLDER_ID", folderId)
                putString("FOLDER_NAME", folderName)
                putBoolean("IS_SHARED_MODE", isSharedMode)
            }
        }

        // 일지 화면으로 프래그먼트 교체
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, journalFragment)
            .addToBackStack(null)
            .commit()

        // ⭐ 폴더를 눌러서 이동했으므로 하단 탭 하이라이트도 '일지'로 강제 업데이트
        updateTabUI(tabJournal)
    }

    /**
     * [일지 상세 리스트 이동] DiaryMainFragment에서 '일지 보기' 클릭 시 호출.
     * 캘린더 화면에서 특정 폴더의 전체 리스트 화면으로 이동.
     */
    fun moveToDiaryList(folderId: Long, folderName: String?, isSharedMode: Boolean) {
        val listFragment = DiaryListFragment().apply {
            arguments = Bundle().apply {
                putLong("FOLDER_ID", folderId)
                putString("FOLDER_NAME", folderName)
                // ⭐ 중요: 리스트 화면에서도 공유 일지/나의 일지 구분을 위해 공유 모드 전달
                putBoolean("IS_SHARED_MODE", isSharedMode)
            }
        }

        // 상세 리스트는 뒤로가기가 가능해야 하므로 .addToBackStack(null) 사용
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_container, listFragment)
            .addToBackStack(null)
            .commit()

        updateTabUI(tabJournal)
    }

    /**
     * 공통 프래그먼트 교체 함수
     * 탭을 이동할 때는 이전 프래그먼트 기록(BackStack)을 모두 비우고 새로 시작함
     */
    private fun replaceFragment(fragment: Fragment) {
        // 백스택 초기화 (탭 간 이동 시 뒤로가기가 꼬이는 것을 방지)
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_container, fragment)
            .commit()
    }

    /**
     * 하단 탭의 시각적 상태(배경, 아이콘 색상, 텍스트 색상)를 업데이트하는 함수
     */
    private fun updateTabUI(selectedTab: LinearLayout) {
        val tabs = listOf(tabFolder, tabJournal, tabMy)

        tabs.forEach { tab ->
            val isSelected = (tab == selectedTab)

            // 선택된 탭은 배경 리소스를 적용하고, 나머지는 투명하게 처리
            tab.setBackgroundResource(if (isSelected) R.drawable.tab_selected else 0)

            // 탭 레이아웃 내부의 0번째 자식(ImageView)과 1번째 자식(TextView) 추출
            val icon = tab.getChildAt(0) as? ImageView
            val text = tab.getChildAt(1) as? TextView

            // 선택 여부에 따른 아이콘 틴트 색상 변경
            icon?.let {
                val color = if (isSelected) Color.BLACK else Color.parseColor("#AAAAAA")
                it.imageTintList = ColorStateList.valueOf(color)
            }

            // 선택 여부에 따른 텍스트 색상 변경
            text?.let {
                it.setTextColor(if (isSelected) Color.BLACK else Color.parseColor("#AAAAAA"))
            }
        }
    }
}