package com.example.healthcareapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutActivity : AppCompatActivity() {

    private var isTimerRunning = false
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerText: TextView
    private lateinit var btnfinish : TextView

    private val timerRunnable = object : Runnable {
        override fun run() {
            seconds++
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            timerText.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
            handler.postDelayed(this, 1000)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

        timerText = findViewById(R.id.tv_main_timer)
        val timerLayout = findViewById<View>(R.id.layout_timer)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        btnfinish = findViewById(R.id.finish_btn)
        // 1. 뷰페이저 어댑터 설정
        viewPager.adapter = ViewPagerAdapter(this)

        // 2. 탭과 뷰페이저 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "운동 기록"
                else -> "컨디션 체크"
            }
        }.attach()

        // 3. 타이머 제어 (Activity가 담당)
        timerLayout.setOnClickListener {
            if (isTimerRunning) {
                handler.removeCallbacks(timerRunnable)
            } else {
                handler.post(timerRunnable)
            }
            isTimerRunning = !isTimerRunning
        }
         // "종료" 텍스트뷰 ID

        btnfinish.setOnClickListener {
            // 1. 타이머 정지
            handler.removeCallbacks(timerRunnable)
            isTimerRunning = false

            // 2. 현재 시간 구하기 (예: 14:06)
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            // 3. ViewPager를 통해 ConditionCheckFragment 찾아가기
            val fragment = supportFragmentManager.findFragmentByTag("f1") as? ConditionCheckFragment
            // (또는 어댑터를 통해 현재 생성된 프래그먼트 인스턴스에 접근)

            fragment?.addNewRecord("운동 후 컨디션 체크", currentTime)

            // 4. 탭 이동 (컨디션 체크 탭으로 자동 전환)
            viewPager.currentItem = 1
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }
}