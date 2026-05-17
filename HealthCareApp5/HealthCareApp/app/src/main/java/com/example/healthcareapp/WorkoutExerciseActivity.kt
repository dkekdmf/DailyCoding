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
    private val timerObserver = androidx.lifecycle.Observer<Int> { time ->
        binding.tvTimer.text = TimerManager.getFormattedTime()
    }
    private val personalWorkoutList = mutableListOf<ExerciseRecord>()
    private val ptWorkoutList = mutableListOf<ExerciseRecord>()

    private var isPtMode = false
    private var startTime: String = ""

    // 🎯 폴더별 저장을 위해 전달받은 폴더 정보를 담을 변수
    private var folderId: Long = -1L
    private var folderName: String? = null

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
        if (result.resultCode == Activity.RESULT_OK) {
            val intentData = result.data
            setResult(Activity.RESULT_OK, intentData)
            TimerManager.pauseTimer()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        folderId = intent.getLongExtra("FOLDER_ID", -1L)
        folderName = intent.getStringExtra("FOLDER_NAME")

        Log.d("JaehoonLog", "🚀 운동 시작 - 폴더 ID: $folderId, 폴더명: $folderName")

        startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        setupRecyclerView()
        setupUI()
        setupTabLogic()
        syncTimer()
    }

    private fun setupUI() {
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        binding.btnClose.setOnClickListener { finish() }

        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
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
        binding.btnPause.setImageResource(if (TimerManager.isRunning()) R.drawable.pause else R.drawable.play)
    }

    private fun showFinishDialog() {
        val dialog = WorkoutFinishDialog {
            moveToFinishActivity()
        }
        dialog.show(supportFragmentManager, "WorkoutFinishDialog")
    }

    private fun moveToFinishActivity() {
        TimerManager.timeLiveData.removeObservers(this)

        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val totalTimerTime = binding.tvTimer.text.toString()

        TimerManager.stopAndResetTimer()

        val intent = Intent(this, WorkoutFinishActivity::class.java).apply {
            putExtra("FOLDER_ID", folderId)
            putExtra("TOTAL_TIME", totalTimerTime)
            putExtra("START_TIME", startTime)
            putExtra("END_TIME", endTime)
            // 🎯 [정밀 교정] 사용자가 머물러 있던 현재 탭 모드 정보를 무결성 배달합니다.
            putExtra("WORKOUT_TYPE", if (isPtMode) "PT" else "개인운동")
        }

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
        super.onDestroy()
    }
}