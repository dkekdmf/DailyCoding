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

class WorkoutRecordFragment : Fragment(R.layout.fragment_workout) {


    private lateinit var workoutAdapter: WorkoutAdapter
    private val workoutList = mutableListOf<ExerciseRecord>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)


        if (workoutList.isEmpty()) {
            workoutList.add(
                ExerciseRecord(1, "벤치프레스 머신", mutableListOf(
                    ExerciseSet(1, 60, 10),
                    ExerciseSet(2, 60, 10)
                ))
            )
        }

        // 어댑터 초기화 및 리사이클러뷰 설정
        workoutAdapter = WorkoutAdapter(workoutList)
        rvWorkout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workoutAdapter
        }
    }


     //WorkoutActivity에서 운동 이름을 받아 리스트에 추가하는 함수

    fun addExerciseToList(exerciseName: String) {
        // 새로운 운동 객체 생성 (기본 1세트 포함)
        val newExercise = ExerciseRecord(
            id = workoutList.size + 1,
            name = exerciseName,
            sets = mutableListOf(ExerciseSet(1, 0, 0)) // 이미지 가이드에 따른 기본 세트 추가
        )

        // 리스트에 추가 및 어댑터 갱신
        workoutList.add(newExercise)

        // UI 업데이트 (어댑터가 초기화된 상태인지 확인 후 갱신)
        if (::workoutAdapter.isInitialized) {
            workoutAdapter.notifyItemInserted(workoutList.size - 1)

            // 추가된 위치로 스크롤 이동
            view?.findViewById<RecyclerView>(R.id.rv_workout_list)?.scrollToPosition(workoutList.size - 1)
        }
    }
}