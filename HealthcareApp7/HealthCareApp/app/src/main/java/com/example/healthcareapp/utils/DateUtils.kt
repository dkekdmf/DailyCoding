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

        // 2. 주차 판정을 위해 '목요일' 날짜 계산 (변수 범위 에러 방지를 위해 미리 계산)
        val calcCal = calendar.clone() as Calendar
        calcCal.add(Calendar.DAY_OF_MONTH, 4) // 일요일(0) + 4 = 목요일

        val year = calcCal.get(Calendar.YEAR)
        val month = calcCal.get(Calendar.MONTH) + 1 // 0부터 시작하므로 +1
        val dayOfThurs = calcCal.get(Calendar.DAY_OF_MONTH)

        // 3. 수학적 주차 계산 (공식: (목요일의 일수 - 1) / 7 + 1)
        val weekOfMonth = (dayOfThurs - 1) / 7 + 1

        // ⭐ title 변수를 여기서 확실히 선언합니다.
        val title = String.format("%d.%02d %d주차", year, month, weekOfMonth)

        // 4. 리스트 데이터 생성
        val dayFormat = SimpleDateFormat("d", Locale.KOREA)
        val dayOfWeekFormat = SimpleDateFormat("E", Locale.KOREA)
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        val weekList = mutableListOf<DayItem>()
        val selectedDateStr = fullDateFormat.format(targetDate)

        // 반복문을 돌리기 전 calendar가 다시 일요일인지 확인 (이미 일요일 상태임)
        for (i in 0..6) {
            val date = calendar.time
            val dateStr = fullDateFormat.format(date)

            weekList.add(
                DayItem(
                    dayOfWeek = dayOfWeekFormat.format(date),
                    dayNumber = dayFormat.format(date),
                    fullDate = dateStr,
                    date = date, // 이제 에러가 나지 않습니다!
                    isSelected = (dateStr == selectedDateStr)
                )
            )
            // 다음 날로 이동
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 이제 title과 weekList 모두 이 범위 내에 존재하므로 에러가 나지 않습니다.
        return Pair(title, weekList)
    }
}