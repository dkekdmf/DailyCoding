package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.RecordWorkoutAdapter
import com.example.healthcareapp.adapter.WorkoutAdapter

import com.example.healthcareapp.databinding.ActivityWorkoutStartBinding
import com.example.healthcareapp.data.WorkoutExercise
import java.text.SimpleDateFormat
import java.util.*

class WorkoutExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutStartBinding
    private lateinit var workoutAdapter: RecordWorkoutAdapter
    private val workoutList = mutableListOf<WorkoutExercise>()

    // 타이머 관련 변수
    private var seconds = 0
    private var isPaused = false
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                seconds++
                binding.tvTimer.text = formatTime(seconds)
            }
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        startTimer()
    }

    private fun setupUI() {
        // 날짜 설정 (26.04.09 형식)
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        // 닫기 버튼
        binding.btnClose.setOnClickListener { finish() }

        // 운동 추가하기 버튼 -> 이전에 만든 AddExerciseActivity 실행
        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
            // 여기서 결과를 받아오는 방식으로 구현하거나, 단순히 이동 후 리스트를 갱신합니다.
            startActivity(intent)
        }

        // 일시정지 버튼
        binding.btnPause.setOnClickListener {
            isPaused = !isPaused
            binding.btnPause.setImageResource(
                if (isPaused) R.drawable.pause else R.drawable.pause
            )
        }

        // 운동 마치기 버튼
        binding.btnFinishWorkout.setOnClickListener {
            // 결과 저장 로직 추가 가능
            finish()
        }
    }

    private fun setupRecyclerView() {
        workoutAdapter = RecordWorkoutAdapter(workoutList)
        binding.rvWorkoutList.apply {
            layoutManager = LinearLayoutManager(this@WorkoutExerciseActivity)
            adapter = workoutAdapter
        }

        // 테스트 데이터 (나중에 AddExerciseActivity에서 전달받은 데이터로 교체)
        addDefaultData()
    }

    private fun addDefaultData() {
        workoutList.add(WorkoutExercise(1, "벤치프레스 머신"))
        workoutList.add(WorkoutExercise(2, "바벨 백스쿼트"))
        workoutAdapter.notifyDataSetChanged()
    }

    private fun startTimer() {
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    private fun formatTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
    }
}