package com.example.healthcareapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.adapter.WorkoutAdapter
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet

/**
 * 운동 세션 중 추가된 운동 종목과 세트 기록을 보여주는 프래그먼트
 */
class WorkoutRecordFragment : Fragment(R.layout.fragment_workout) {

    private lateinit var workoutAdapter: WorkoutAdapter
    private val workoutList = mutableListOf<ExerciseRecord>() // 화면에 표시할 운동 리스트 데이터

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)

        // =========================================
        // 🎯 [데이터 공급처 교체] 2. 부모 액티비티가 복원해온 진짜 유저 세트 정보 수신
        // ==========================================
        workoutList.clear()
        val parentActivity = activity as? WorkoutActivity
        val realSavedList = parentActivity?.savedExerciseList

        if (realSavedList != null && realSavedList.isNotEmpty()) {
            Log.d("JaehoonTest", "🔥 WorkoutRecordFragment: 부모로부터 실제 세트 데이터 ${realSavedList.size}건 이식 완료")
            workoutList.addAll(realSavedList)
        } else {
            // 💾 만약 저장된 데이터가 진짜로 아예 없는 빈 일지일 때만 예외 방어용 기본 안내 더미 배치
            if (workoutList.isEmpty()) {
                workoutList.add(
                    ExerciseRecord(1, "벤치프레스 머신", mutableListOf(
                        ExerciseSet(1, 60, 10),
                        ExerciseSet(2, 60, 10)
                    ))
                )
            }
        }

        // 3. 어댑터 초기화 및 리사이클러뷰 연결
        workoutAdapter = WorkoutAdapter(workoutList)
        rvWorkout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workoutAdapter
        }
    }

    /**
     * [Activity에서 호출용] 운동 선택 화면에서 골라온 운동 이름을 리스트에 동적으로 추가함
     */
    fun addExerciseToList(exerciseName: String) {
        val newExercise = ExerciseRecord(
            id = workoutList.size + 1,
            name = exerciseName,
            sets = mutableListOf(ExerciseSet(1, 0, 0))
        )

        workoutList.add(newExercise)

        if (::workoutAdapter.isInitialized) {
            workoutAdapter.notifyItemInserted(workoutList.size - 1)
            view?.findViewById<RecyclerView>(R.id.rv_workout_list)?.scrollToPosition(workoutList.size - 1)
        }
    }
}