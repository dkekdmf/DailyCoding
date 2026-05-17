package com.example.healthcareapp

import android.content.Context
import com.example.healthcareapp.data.JournalSummaryDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DiaryPrefsManager {
    private const val PREF_NAME = "HealthDiaryPrefs"
    private fun getFolderKey(folderId: Long) = "diary_list_$folderId"

    fun saveJournal(context: Context, folderId: Long, journal: JournalSummaryDto) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existingList = getJournals(context, folderId).toMutableList()
        existingList.add(journal) // 🎯 기존 데이터 뒤에 추가 (누적)

        val json = Gson().toJson(existingList)
        prefs.edit().putString(getFolderKey(folderId), json).apply()
    }

    fun getJournals(context: Context, folderId: Long): List<JournalSummaryDto> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(getFolderKey(folderId), null) ?: return emptyList()
        val type = object : TypeToken<List<JournalSummaryDto>>() {}.type
        return Gson().fromJson(json, type)
    }
    fun clearFolderJournals(context: Context, folderId: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // 💾 해당 폴더의 Key 자체를 지워버려 데이터를 깔끔하게 공중분해 시킵니다.
        prefs.edit().remove(getFolderKey(folderId)).apply()
    }
    fun saveAllJournals(context: Context, folderId: Long, journals: List<JournalSummaryDto>) {
        try {
            // 기존에 일지 데이터를 저장하던 파일명 규격("HealthDiaryPrefs")과 매칭
            val prefs = context.getSharedPreferences("HealthDiaryPrefs", Context.MODE_PRIVATE)

            // GSON을 활용해 객체 리스트를 JSON 문자열로 변환
            val gson = com.google.gson.Gson()
            val json = gson.toJson(journals)

            // 기존 폴더 전용 키값 규격("diary_list_폴더ID")에 그대로 덮어쓰기
            prefs.edit().putString("diary_list_$folderId", json).apply()


        } catch (e: Exception) {

        }
    }
}