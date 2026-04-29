package com.example.healthcareapp.utils

import com.example.healthcareapp.data.DayItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
object DateUtils {
    fun getWeekInfo(targetDate: Date = Date()): Pair<String, List<DayItem>> {
        val calendar = Calendar.getInstance(Locale.KOREA)
        calendar.time = targetDate

        // 1. 해당 주의 일요일로 이동 (이번 주의 시작점)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val sundayDate = calendar.time

        // 2. 주차 판정 기준: 이번 주의 '목요일'을 구함
        val calcCal = calendar.clone() as Calendar
        calcCal.add(Calendar.DAY_OF_MONTH, 4) // 일(0) + 4 = 목요일

        val year = calcCal.get(Calendar.YEAR)
        val month = calcCal.get(Calendar.MONTH) + 1 // 0부터 시작하므로 +1
        val dayOfMonth = calcCal.get(Calendar.DAY_OF_MONTH)

        // 3. 수학적 주차 계산 (시스템 WEEK_OF_MONTH 무시)
        // 공식: (목요일의 일수 - 1) / 7 + 1
        val weekOfMonth = (dayOfMonth - 1) / 7 + 1

        val title = String.format("%d.%02d %d주차", year, month, weekOfMonth)

        // 4. 리스트 데이터 생성 (일요일부터 토요일까지)
        val dayFormat = SimpleDateFormat("d", Locale.KOREA)
        val dayOfWeekFormat = SimpleDateFormat("E", Locale.KOREA)
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        val weekList = mutableListOf<DayItem>()
        val selectedDateStr = fullDateFormat.format(targetDate)

        // 이미 1번에서 calendar가 일요일로 맞춰져 있으므로 그대로 반복문 실행
        for (i in 0..6) {
            val date = calendar.time
            val dateStr = fullDateFormat.format(date)
            weekList.add(
                DayItem(
                    dayOfWeek = dayOfWeekFormat.format(date),
                    dayNumber = dayFormat.format(date),
                    fullDate = dateStr,
                    isSelected = (dateStr == selectedDateStr)
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return Pair(title, weekList)
    }
}