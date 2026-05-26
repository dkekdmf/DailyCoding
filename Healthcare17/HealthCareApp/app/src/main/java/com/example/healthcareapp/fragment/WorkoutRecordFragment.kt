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

class WorkoutRecordFragment : Fragment(R.layout.fragment_workout) {

    private lateinit var workoutAdapter: WorkoutAdapter
    private val workoutList = mutableListOf<ExerciseRecord>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // view가 null이 아님을 보장하므로 안전합니다.
        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)

        val parentActivity = activity as? WorkoutActivity

        // 데이터 이식 로직
        if (workoutList.isEmpty()) {
            val realSavedList = parentActivity?.savedExerciseList
            if (!realSavedList.isNullOrEmpty()) {
                Log.d("JaehoonTest", "🔥 WorkoutRecordFragment: 부모 복원 데이터 ${realSavedList.size}건 이식")
                workoutList.addAll(realSavedList)
            } else {
                workoutList.add(
                    ExerciseRecord(1, "벤치프레스 머신", mutableListOf(
                        ExerciseSet(1, 60, 10),
                        ExerciseSet(2, 60, 10)
                    ))
                )
            }
        }

        // 어댑터 초기화 (context가 확실히 존재하는 시점)
        workoutAdapter = WorkoutAdapter(workoutList)
        rvWorkout.layoutManager = LinearLayoutManager(context)
        rvWorkout.adapter = workoutAdapter

        setEditable(false)
    }

    fun setEditable(editable: Boolean) {
        // 프래그먼트가 현재 Context에 붙어있는지 확인 후 실행 (크래시 방지)
        if (isAdded && ::workoutAdapter.isInitialized) {
            workoutAdapter.isEditMode = editable
            workoutAdapter.notifyDataSetChanged()
            Log.d("JaehoonEdit", "🏋️ 운동기록 수정 모드 변경 -> 활성화: $editable")
        }
    }

    fun addExerciseToList(exerciseName: String) {
        // 프래그먼트가 사라진 상태에서 호출될 경우를 대비한 안전 체크
        if (!isAdded) return

        val newExercise = ExerciseRecord(
            id = (workoutList.maxOfOrNull { it.id } ?: 0) + 1,
            name = exerciseName,
            sets = mutableListOf(ExerciseSet(1, 0, 0))
        )

        workoutList.add(newExercise)

        if (::workoutAdapter.isInitialized) {
            workoutAdapter.notifyItemInserted(workoutList.size - 1)
            // view?.findViewById 호출보다는 이미 rv를 찾을 수 있는 경우 활용 추천
            view?.findViewById<RecyclerView>(R.id.rv_workout_list)?.scrollToPosition(workoutList.size - 1)
        }
    }

    fun saveUpdatedExerciseData() {
        Log.d("JaehoonEdit", "💾 [운동 기록 데이터 동기화] 수정 상태 유지 중.")
    }
}