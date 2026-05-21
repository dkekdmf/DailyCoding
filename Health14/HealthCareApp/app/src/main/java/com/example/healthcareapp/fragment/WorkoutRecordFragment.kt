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

        // 🎯 [최초 가드링] 상세 화면 최초 진입 시점에는 무조건 무게/횟수가 잠겨있도록 안전하게 락을 겁니다.
        setEditable(false)
    }

    /**
     * 🎯 [신규 추가] 부모 액티비티(WorkoutActivity)의 수정 버튼 상태에 따라 운동 기록 락을 온오프하는 마스터 채널
     */
    fun setEditable(editable: Boolean) {
        if (::workoutAdapter.isInitialized) {
            // 1. 상위 어댑터의 마스터 스위치 플래그 동기화
            workoutAdapter.isEditMode = editable

            // 2. 🚨 리사이클러뷰에게 "무게, 횟수 입력창 상태 바꼈으니 다시 그려!" 명령 호출
            workoutAdapter.notifyDataSetChanged()
        }
        Log.d("JaehoonEdit", "🏋️ 운동기록 프래그먼트 수정 모드 상태 가동 -> 현재 활성화: $editable")
    }

    /**
     * 🎯 [신규 추가] 부모가 [완료] 버튼을 눌렀을 때 최종 수정본 저장 처리를 위한 징검다리 인터페이스 함수
     */
    fun saveUpdatedExerciseData() {
        // 💡 필요 시 여기에 SharedPreferences 덮어쓰기 로직을 확장해 넣을 수 있도록 통로를 개설해 둡니다.
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