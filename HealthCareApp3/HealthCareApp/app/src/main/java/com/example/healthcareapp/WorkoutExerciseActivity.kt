package com.example.healthcareapp

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.RecordWorkoutAdapter
import com.example.healthcareapp.databinding.ActivityWorkoutStartBinding
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 실제 운동을 기록하는 화면 (타이머, 세트 기록, 운동 추가)
 */
class WorkoutExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutStartBinding
    private lateinit var workoutAdapter: RecordWorkoutAdapter

    private val personalWorkoutList = mutableListOf<ExerciseRecord>()
    private val ptWorkoutList = mutableListOf<ExerciseRecord>()
    private val timerObserver = androidx.lifecycle.Observer<Int> { time ->
        binding.tvTimer.text = TimerManager.getFormattedTime()
    }
    private var isPtMode = false
    private var startTime: String = ""

    private val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val names = result.data?.getStringArrayExtra("exercise_names")
            names?.forEach { name -> addNewExercise(name) }
        }
    }
    private val finishResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("JaehoonLog", "finishResultLauncher 결과 수신됨. 코드: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            TimerManager.stopAndResetTimer()

            val intentData = result.data
            setResult(Activity.RESULT_OK, intentData)

            Log.d("JaehoonLog", "WorkoutExerciseActivity를 완전히 finish() 합니다.")
            finish() // 🚨 이 줄이 실행되면서 화면이 아예 사라져야 합니다!
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🚨 [타임존 적용] 최초 onCreate 시점에도 한국 시간 기준으로 백업해둡니다.
        val sdfSeoul = SimpleDateFormat("HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        startTime = sdfSeoul.format(Date())

        setupRecyclerView()
        setupUI()
        setupTabLogic()
        syncTimer()
    }

    private fun setupUI() {
        // 🚨 한국 표준시 타임존 설정 적용
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        binding.tvDate.text = sdf.format(Date())

        binding.btnClose.setOnClickListener { finish() }

        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
            intent.putExtra("IS_PLAY_MODE", isPtMode) // 필요한 기존 키값 유지
            intent.putExtra("IS_PT_MODE", isPtMode)
            addExerciseLauncher.launch(intent)
        }

        binding.btnPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                binding.btnPause.setImageResource(R.drawable.play)
            } else {
                TimerManager.startTimer()
                binding.btnPause.setImageResource(R.drawable.pause)
            }
        }

        binding.btnFinishWorkout.setOnClickListener {
            showFinishDialog()
        }
    }

    private fun syncTimer() {
        TimerManager.timeLiveData.observe(this, timerObserver)

        if (TimerManager.isRunning()) {
            binding.btnPause.setImageResource(R.drawable.pause)
        } else {
            binding.btnPause.setImageResource(R.drawable.play)
        }
    }

    private fun showFinishDialog() {
        val dialog = WorkoutFinishDialog {
            moveToFinishActivity()
        }
        dialog.show(supportFragmentManager, "WorkoutFinishDialog")
    }

    private fun moveToFinishActivity() {
        if (TimerManager.isRunning()) {
            TimerManager.pauseTimer()
        }

        // 1. 루프가 멈춘 상태의 최종 타이머 텍스트 확보
        val totalTimerTime = binding.tvTimer.text.toString()

        // 2. 🚨 [종료 시간 강제 한국 타임존 고정]
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val now = Date()
        val endTime = sdf.format(now)

        // 3. 🚨 [시간 오염 원천 차단 역산 알고리즘]
        // 기기(에뮬레이터) 타임존 문제로 startTime이 15시대로 오염되는 것을 완벽히 방어합니다.
        // 한국 표준시 종료 시간에서 총 운동 시간(초)만큼 뒤로 감아 진짜 한국 기준 시작 시간을 직접 도출합니다.
        val realStartTime = try {
            val parts = totalTimerTime.split(":")
            val totalSeconds = (parts[0].toInt() * 3600) + (parts[1].toInt() * 60) + parts[2].toInt()

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA).apply {
                time = now
                add(Calendar.SECOND, -totalSeconds)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            // 역산 예외 발생 시 차선책으로 기본 지정된 startTime 활용
            startTime
        }

        // 4. 인텐트에 기록 데이터 탑재
        val intent = Intent(this, WorkoutFinishActivity::class.java)
        intent.putExtra("TOTAL_TIME", totalTimerTime)
        intent.putExtra("START_TIME", realStartTime) // 👈 역산 완료된 진짜 00시대 시작 시각 전달!
        intent.putExtra("END_TIME", endTime)         // 👈 한국 표준시 종료 시각 전달!

        val workoutType = if (isPtMode) "PT" else "개인운동"

        // 🚨 드디어 전면 화면에서 마치기를 누를 때도 완벽히 교정된 자정 로그가 찍힙니다!
        Log.d("JaehoonLog", "[전송직전] 타입: $workoutType, 시작: $realStartTime, 종료: $endTime, 총시간: $totalTimerTime")
        intent.putExtra("WORKOUT_TYPE", workoutType)

        // 5. 결과를 기다리도록 런처로 실행
        finishResultLauncher.launch(intent)
    }

    private fun setupTabLogic() {
        updateTabUI()
        binding.tvTabPersonal.setOnClickListener {
            if (isPtMode) {
                isPtMode = false
                Log.d("JaehoonTest", "[탭클릭] 개인운동 선택됨 (isPtMode: $isPtMode)")
                updateTabUI()
                switchListData()
            }
        }
        binding.tvTabPt.setOnClickListener {
            if (!isPtMode) {
                isPtMode = true
                Log.d("JaehoonTest", "[탭클릭] PT 선택됨 (isPtMode: $isPtMode)")
                updateTabUI()
                switchListData()
            }
        }
    }

    private fun updateTabUI() {
        binding.tvTabPersonal.isSelected = !isPtMode
        binding.tvTabPt.isSelected = isPtMode

        if (isPtMode) {
            binding.tvTabPt.paint.isFakeBoldText = true
            binding.tvTabPersonal.paint.isFakeBoldText = false
        } else {
            binding.tvTabPersonal.paint.isFakeBoldText = true
            binding.tvTabPt.paint.isFakeBoldText = false
        }
        binding.btnAddWorkout.text = "+ 운동 추가하기"
    }

    private fun switchListData() {
        val currentList = if (isPtMode) ptWorkoutList else personalWorkoutList
        workoutAdapter = RecordWorkoutAdapter(currentList)
        binding.rvWorkoutList.adapter = workoutAdapter
        binding.rvWorkoutList.scheduleLayoutAnimation()
    }

    private fun setupRecyclerView() {
        workoutAdapter = RecordWorkoutAdapter(personalWorkoutList)
        binding.rvWorkoutList.apply {
            layoutManager = LinearLayoutManager(this@WorkoutExerciseActivity)
            adapter = workoutAdapter
        }
    }

    private fun addNewExercise(name: String) {
        val currentList = if (isPtMode) ptWorkoutList else personalWorkoutList
        val newWorkout = ExerciseRecord(
            id = currentList.size + 1,
            name = name,
            sets = mutableListOf(ExerciseSet(setNumber = 1, weight = 0, reps = 0))
        )
        currentList.add(newWorkout)
        workoutAdapter.notifyItemInserted(currentList.size - 1)
        binding.rvWorkoutList.scrollToPosition(currentList.size - 1)
    }
    override fun onDestroy() {
        TimerManager.timeLiveData.removeObserver(timerObserver)
        binding.tvTimer.text = ""
        super.onDestroy()
    }
}