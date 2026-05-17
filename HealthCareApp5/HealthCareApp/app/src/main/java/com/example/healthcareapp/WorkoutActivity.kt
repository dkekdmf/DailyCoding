package com.example.healthcareapp

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WorkoutActivity : AppCompatActivity() {

    // 드롭다운 상태(열림/닫힘)를 추적하기 위한 플래그 변수
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 운동 시작/기록 화면 레이아웃 설정
        setContentView(R.layout.exercise_start)

        // 1. UI 컴포넌트 찾아오기
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)

        // 상단 헤더의 날짜와 이모티콘 뷰
        val tvHeaderDate = findViewById<TextView>(R.id.tv_date_header)
        val ivHeaderEmoji = findViewById<ImageView>(R.id.condition_emoticon)

        val conditionname = "컨디션 체크"

        // 2. ⭐ 데이터 수신 및 로그 확인 (폴더 ID 수신 추가)
        val diaryDate = intent.getStringExtra("DIARY_DATE") ?: "날짜 없음"
        val startTab = intent.getIntExtra("SELECT_TAB", 0)

        // 🎯 [규칙 1, 2 연동 핵심] 이 일지가 속한 고유 폴더 ID 수신 (기본값 -1L)
        // DiaryListFragment의 인텐트 포장 방식(Long 타입)과 매칭을 유도합니다.
        val intentFolderId = intent.getStringExtra("FOLDER_ID")?.toLongOrNull()
            ?: intent.getLongExtra("FOLDER_ID", -1L)

        // 1차 인텐트 다이렉트 이모티콘 리소스 복원
        var emojiResId = intent.getIntExtra("EMOJI_RES_ID", -1)

        Log.d("JaehoonTest", "--- WorkoutActivity 데이터 수신 확인 ---")
        Log.d("JaehoonTest", "수신된 날짜: $diaryDate, 폴더 ID: $intentFolderId")
        Log.d("JaehoonTest", "인텐트 다이렉트 이모티콘 ID: $emojiResId")

        // 🎯 [3단 싱크 보장 핵심] SharedPreferences 저장소에서 이 폴더 전용 고유 키로 더블 체크 검증을 실행합니다!
        if (intentFolderId != -1L && diaryDate != "날짜 없음") {
            val sharedPrefs = getSharedPreferences("CalendarEmojiPrefs", Context.MODE_PRIVATE)
            // 프래그먼트와 한치의 오차도 없는 공통 포맷 키 조합 생성
            val folderIsolatedKey = "${diaryDate}_$intentFolderId"
            val cachedEmojiId = sharedPrefs.getInt(folderIsolatedKey, -1)

            if (cachedEmojiId != -1) {
                Log.d("JaehoonTest", "💾 폴더 격리 프리프 일치화 캐시 발견 -> 기존 값 교체 고정: $cachedEmojiId")
                emojiResId = cachedEmojiId // 프리프 전용 파일의 최신 이모티콘 색상으로 지배 선언
            }
        }

        // 3. ⭐ 받아온 데이터를 상단 헤더에 적용 (yyyy.MM.dd 점 서식 교정 포함)
        val formattedDate = if (diaryDate.contains("-")) diaryDate.replace("-", ".") else diaryDate
        tvHeaderDate.text = formattedDate

        if (emojiResId != -1) {
            // 정상적으로 전달받은 경우 (캘린더바, 일지 리스트 이모티콘과 100% 동일색상 바인딩)
            ivHeaderEmoji.setImageResource(emojiResId)
            ivHeaderEmoji.visibility = View.VISIBLE
            Log.d("JaehoonTest", "최종 이모티콘 동기화 적용 성공")
        } else {
            // 데이터를 못 받은 경우 (기본 이미지 세팅 및 경고 로그)
            Log.e("JaehoonTest", "⚠️ EMOJI_RES_ID가 전달되지 않았습니다. 기본 이미지를 사용합니다.")
            ivHeaderEmoji.setImageResource(R.drawable.emoticon1)
            ivHeaderEmoji.visibility = View.VISIBLE
        }

        // 4. 타이머 데이터 더미 세팅
        tvMainTimer.text = "01:12:32"
        tvStartTime.text = "16:16"
        tvEndTime.text = "17:30"

        // 5. 뷰페이저 어댑터 연결
        viewPager.adapter = ViewPagerAdapter(this)

        // 6. TabLayout과 ViewPager2 결합
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView
            customView.text = if (position == 0) "운동 기록" else conditionname

            if (position == 0) {
                customView.setTextColor(ContextCompat.getColor(this, R.color.black))
                customView.setTypeface(null, Typeface.BOLD)
            } else {
                customView.setTextColor(ContextCompat.getColor(this, R.color.chip_selected))
                customView.setTypeface(null, Typeface.NORMAL)
            }
            tab.customView = customView
        }.attach()

        // 7. 탭 선택 시 스타일 변경 리스너
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.black))
                    setTypeface(null, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.chip_selected))
                    setTypeface(null, Typeface.NORMAL)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 8. 전달받은 포지션으로 탭 즉시 이동
        viewPager.post {
            viewPager.setCurrentItem(startTab, false)
        }

        // 9. 뒤로가기 버튼
        arrowBtn.setOnClickListener {
            finish()
        }
    }
}