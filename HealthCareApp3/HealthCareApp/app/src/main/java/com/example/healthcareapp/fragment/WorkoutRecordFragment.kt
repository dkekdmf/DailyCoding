package com.example.healthcareapp.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
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

        // 1. 리사이클러뷰 바인딩
        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)

        // 2. 초기 더미 데이터 설정 (리스트가 비어있을 때만 샘플 추가)
        if (workoutList.isEmpty()) {
            workoutList.add(
                ExerciseRecord(1, "벤치프레스 머신", mutableListOf(
                    ExerciseSet(1, 60, 10),
                    ExerciseSet(2, 60, 10)
                ))
            )
        }

        // 3. 어댑터 초기화 및 리사이클러뷰 연결
        workoutAdapter = WorkoutAdapter(workoutList)
        rvWorkout.apply {
            // 세로 방향 리스트 레이아웃 매니저 설정
            layoutManager = LinearLayoutManager(context)
            adapter = workoutAdapter
        }
    }

    /**
     * [Activity에서 호출용] 운동 선택 화면에서 골라온 운동 이름을 리스트에 동적으로 추가함
     * @param exerciseName 추가할 운동 종목 명
     */
    fun addExerciseToList(exerciseName: String) {
        // 새로운 운동 객체 생성 (기본으로 1세트를 포함하여 사용자 편의성 제공)
        val newExercise = ExerciseRecord(
            id = workoutList.size + 1,
            name = exerciseName,
            sets = mutableListOf(ExerciseSet(1, 0, 0)) // 무게 0, 횟수 0으로 초기 세트 구성
        )

        // 데이터 리스트에 추가
        workoutList.add(newExercise)

        // UI 업데이트 (lateinit 어댑터가 초기화되었는지 안전하게 확인 후 갱신)
        if (::workoutAdapter.isInitialized) {
            // 전체 갱신(notifyDataSetChanged) 대신 마지막 아이템 삽입만 알려서 성능 최적화
            workoutAdapter.notifyItemInserted(workoutList.size - 1)

            // 사용자가 추가된 항목을 바로 볼 수 있도록 리스트의 가장 하단으로 스크롤 이동
            view?.findViewById<RecyclerView>(R.id.rv_workout_list)?.scrollToPosition(workoutList.size - 1)
        }
    }
}