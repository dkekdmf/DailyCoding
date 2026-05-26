package com.example.healthcareapp.data

data class ConditionRecord(
    var title: String,
    val questions: List<StatusQuestion1>, // 🎯 기존대로 1 붙인 타입 유지 완료!
    var time: String = "12:34",          // 🎯 [신규 추가] 9:40 파싱 버그를 원천 차단할 청정 시간 필드
    var memo: String = "",
    var score: Float = 0f,
    var painTag: String = "",
    var isExpanded: Boolean = false,
    var isShowAllQuestions: Boolean = false
)