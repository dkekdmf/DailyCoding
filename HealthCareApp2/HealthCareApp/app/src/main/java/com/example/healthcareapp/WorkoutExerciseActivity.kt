package com.example.healthcareapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.RecordWorkoutAdapter
import com.example.healthcareapp.databinding.ActivityWorkoutStartBinding
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet
import java.text.SimpleDateFormat
import java.util.*

class WorkoutExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutStartBinding
    private lateinit var workoutAdapter: RecordWorkoutAdapter
    private val workoutList = mutableListOf<ExerciseRecord>()

    // 시작 시각을 저장할 변수
    private var startTime: String = ""

    private val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val names = result.data?.getStringArrayExtra("exercise_names")
            names?.forEach { name -> addNewExercise(name) }
        }
    }

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

        // 1. 운동 시작 시각 기록 (HH:mm 형식)
        startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        setupRecyclerView()
        setupUI()
        startTimer()
    }

    private fun setupUI() {
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        binding.btnClose.setOnClickListener { finish() }

        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
            addExerciseLauncher.launch(intent)
        }

        binding.btnPause.setOnClickListener { isPaused = !isPaused }

        // 운동 마치기 버튼 클릭 시 데이터 전달
        binding.btnFinishWorkout.setOnClickListener {
            val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val totalTimerTime = binding.tvTimer.text.toString()

            val intent = Intent(this, WorkoutFinishActivity::class.java)
            intent.putExtra("TOTAL_TIME", totalTimerTime)
            intent.putExtra("START_TIME", startTime)
            intent.putExtra("END_TIME", endTime)

            // 필요 시 Parcelable을 통해 운동 데이터도 전달 가능
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        workoutAdapter = RecordWorkoutAdapter(workoutList)
        binding.rvWorkoutList.apply {
            layoutManager = LinearLayoutManager(this@WorkoutExerciseActivity)
            adapter = workoutAdapter
        }
    }

    private fun addNewExercise(name: String) {
        val newWorkout = ExerciseRecord(
            id = workoutList.size + 1,
            name = name,
            sets = mutableListOf(ExerciseSet(setNumber = 1, weight = 0, reps = 0))
        )
        workoutList.add(newWorkout)
        workoutAdapter.notifyItemInserted(workoutList.size - 1)
        binding.rvWorkoutList.scrollToPosition(workoutList.size - 1)
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