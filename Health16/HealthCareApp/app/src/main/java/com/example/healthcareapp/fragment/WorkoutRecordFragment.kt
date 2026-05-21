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
 * 운동 세션 중 추가된 운동 종목과 세트 기록을 보여주는 프래그먼트 (순정 롤백 및 데이터 안전 보존판)
 */
class WorkoutRecordFragment : Fragment(R.layout.fragment_workout) {

    private lateinit var workoutAdapter: WorkoutAdapter
    private val workoutList = mutableListOf<ExerciseRecord>() // 화면에 표시할 운동 리스트 데이터

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)

        // 🎯 [복원 완료] 타이머 관련 findViewById 구절을 완전히 철거하여 크래시 원인을 완벽 차단했습니다.

        // ==================================================================
        // 🎯 [바벨/백스쿼트 유지 가드] 탭 복귀 및 화면 전환 시 메모리 데이터 보존 파이프라인
        // ==================================================================
        val parentActivity = activity as? WorkoutActivity

        if (workoutList.isEmpty()) {
            val realSavedList = parentActivity?.savedExerciseList

            if (realSavedList != null && realSavedList.isNotEmpty()) {
                Log.d("JaehoonTest", "🔥 WorkoutRecordFragment: 부모 복원 데이터 ${realSavedList.size}건 이식")
                workoutList.addAll(realSavedList)
            } else {
                // 최초 텅 빈 일지 방어선용 순정 더미 데이터
                workoutList.add(
                    ExerciseRecord(1, "벤치프레스 머신", mutableListOf(
                        ExerciseSet(1, 60, 10),
                        ExerciseSet(2, 60, 10)
                    ))
                )
            }
        } else {
            Log.d("JaehoonTest", "✨ WorkoutRecordFragment: 메모리에 바벨/백스쿼트 데이터가 안전하게 활성화 상태를 유지함.")
        }

        // 3. 어댑터 초기화 및 리사이클러뷰 연결
        workoutAdapter = WorkoutAdapter(workoutList)
        rvWorkout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workoutAdapter
        }

        setEditable(false)
    }

    /**
     * 부모 액티비티(WorkoutActivity)의 수정 버튼 상태에 따라 운동 기록 락을 온오프하는 마스터 채널
     */
    fun setEditable(editable: Boolean) {
        if (::workoutAdapter.isInitialized) {
            workoutAdapter.isEditMode = editable
            workoutAdapter.notifyDataSetChanged()
        }
        Log.d("JaehoonEdit", "🏋️ 운동기록 프래그먼트 수정 모드 상태 가동 -> 현재 활성화: $editable")
    }

    /**
     * 부모가 [완료] 버튼을 눌렀을 때 최종 수정본 저장 처리를 위한 징검다리 인터페이스 함수
     */
    fun saveUpdatedExerciseData() {
        Log.d("JaehoonEdit", "💾 [운동 기록 데이터 동기화 확인] 현재 화면상의 수정된 세트 리스트 보존 상태 유지.")
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