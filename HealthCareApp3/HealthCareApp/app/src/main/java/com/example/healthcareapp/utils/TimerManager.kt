package com.example.healthcareapp.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

object TimerManager {
    private var seconds = 0
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    val timeLiveData = MutableLiveData<Int>()

    // 🚨 [새로 추가] 운동 시작 시간을 기록할 변수
    private var workoutStartTimeStr: String = "00:00:00"

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                timeLiveData.value = seconds
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun startTimer() {
        if (!isRunning) {
            if (seconds == 0 || workoutStartTimeStr == "00:00:00") {
                // 🚨 SimpleDateFormat에 한국 시간대(Asia/Seoul)를 강제로 꽂아버립니다.
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.KOREA).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Seoul")
                }
                workoutStartTimeStr = sdf.format(Date())
                Log.d("JaehoonLog", "TimerManager: 한국 시간 고정 완료 -> $workoutStartTimeStr")
            }

            isRunning = true
            handler.removeCallbacks(runnable)
            handler.post(runnable)
        }
    }

    // ⭐ 추가: 타이머 일시정지 (시간은 유지하고 루프만 멈춤)
    fun pauseTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
    }

    // 타이머 완전히 종료 (초기화)
    fun stopTimer() {
        pauseTimer()           // 카운트다운/업 루프 멈추기
        seconds = 0            // 🚨 [누락 수정] 물리적 시간도 0으로 초기화해야 완전히 리셋됩니다!
        timeLiveData.value = 0 // 시간을 0으로 초기화

        // 🚨 [핵심 추가] 타이머가 파괴되었으므로 기록된 시작 시간도 초기화합니다.
        workoutStartTimeStr = "00:00:00"
    }

    fun stopAndResetTimer() {
        pauseTimer()
        seconds = 0            // 🚨 [누락 수정] 물리적 시간도 0으로 완전히 초기화합니다.
        timeLiveData.postValue(0)

        // 🚨 [핵심 추가] 타이머가 파괴되었으므로 기록된 시작 시간도 초기화합니다.
        workoutStartTimeStr = "00:00:00"

        Log.d("JaehoonLog", "TimerManager: 타이머가 완전히 초기화되었습니다.")
    }

    // 🚨 [새로 추가] 프래그먼트나 액티비티에서 진짜 시작 시간을 꺼내갈 수 있게 만들어주는 함수
    fun getWorkoutStartTime(): String {
        return workoutStartTimeStr
    }

    fun getFormattedTime(): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // ⭐ 수정: 현재 타이머가 돌아가고 있는지 확인하는 함수
    fun isRunning(): Boolean = isRunning

    // 타이머 기록이 있는지 확인
    fun isTimerActive(): Boolean = seconds > 0
}