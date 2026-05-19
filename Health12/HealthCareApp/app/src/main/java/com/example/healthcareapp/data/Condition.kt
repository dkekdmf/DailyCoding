package com.example.healthcareapp.data

// 🎯 각 세션별 기록을 식별하기 위한 DTO 구조 예시
data class ConditionSession(
    val idName: String,         // 🎯 여기에 "컨디션체크 01", "컨디션체크 02"가 자동 저장됩니다.
    val scores: List<Float>,
    val memo: String,
    val painTag: String,
    val time: String            // 기록된 시간 (HH:mm)
)

data class WorkoutSession(
    val idName: String,         // 🎯 여기에 "운동마치기 01", "운동마치기 02"가 자동 저장됩니다.
    val totalTime: String,
    val exerciseList: List<ExerciseItem>, // 실제 운동 종목 리스트
    val time: String
)

// 🎯 최종적으로 저장될 오늘 날짜의 마스터 일지 DTO
data class DailyJournalDto(
    val folderId: Long,
    val date: String,           // "2026-05-19" (날짜 기준 묶음키)
    val workoutSessions: MutableList<WorkoutSession> = mutableListOf(), // 운동마치기 리스트
    val conditionSessions: MutableList<ConditionSession> = mutableListOf() // 컨디션체크 리스트
)