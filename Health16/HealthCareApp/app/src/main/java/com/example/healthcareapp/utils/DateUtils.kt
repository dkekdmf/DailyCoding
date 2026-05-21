package com.example.healthcareapp.utils

import com.example.healthcareapp.data.DayItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
object DateUtils {
    // 🎯 folderId를 매개변수로 받도록 수정합니다.
    fun getWeekInfo(targetDate: Date = Date(), folderId: Long): Pair<String, List<DayItem>> {
        val calendar = Calendar.getInstance(Locale.KOREA)
        calendar.time = targetDate

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val calcCal = calendar.clone() as Calendar
        calcCal.add(Calendar.DAY_OF_MONTH, 4)

        val year = calcCal.get(Calendar.YEAR)
        val month = calcCal.get(Calendar.MONTH) + 1
        val dayOfThurs = calcCal.get(Calendar.DAY_OF_MONTH)

        val weekOfMonth = (dayOfThurs - 1) / 7 + 1
        val title = String.format("%d.%02d %d주차", year, month, weekOfMonth)

        val dayFormat = SimpleDateFormat("d", Locale.KOREA)
        val dayOfWeekFormat = SimpleDateFormat("E", Locale.KOREA)
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        val weekList = mutableListOf<DayItem>()
        val selectedDateStr = fullDateFormat.format(targetDate)

        for (i in 0..6) {
            val date = calendar.time
            val dateStr = fullDateFormat.format(date)

            weekList.add(
                DayItem(
                    dayOfWeek = dayOfWeekFormat.format(date),
                    dayNumber = dayFormat.format(date),
                    fullDate = dateStr,
                    date = date,
                    isSelected = (dateStr == selectedDateStr),
                    folderId = folderId // 👈 매개변수로 받은 folderId를 사용!
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return Pair(title, weekList)
    }
}