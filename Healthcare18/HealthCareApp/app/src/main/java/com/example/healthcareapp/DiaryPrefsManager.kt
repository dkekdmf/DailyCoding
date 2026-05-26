package com.example.healthcareapp

import android.content.Context
import com.example.healthcareapp.data.JournalSummaryDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DiaryPrefsManager {
    private const val PREF_NAME = "HealthDiaryPrefs"
    private fun getFolderKey(folderId: Long) = "diary_list_$folderId"

    /**
     * 🎯 [스마트 통합 저장 엔진]
     * 동일한 ID를 가진 일지가 이미 있으면 내부 데이터를 '수정(Update)'하고,
     * 아예 새로운 일지라면 리스트 끝에 '추가(Insert)'하여 중복 데이터 폭탄을 원천 차단합니다.
     */
    fun saveJournal(context: Context, folderId: Long, journal: JournalSummaryDto) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existingList = getJournals(context, folderId).toMutableList()

        // 리스트 내부에 동일한 ID가 있는지 인덱스 추적
        val targetIndex = existingList.indexOfFirst { it.id == journal.id }

        if (targetIndex != -1) {
            // 🚀 1. 이미 존재하면 최신 수정본(컨디션 메모, 통증 등)으로 스마트 덮어쓰기!
            existingList[targetIndex] = journal
        } else {
            // 🚀 2. 완전히 처음 쓰는 일지 세션이면 안전하게 신규 누적 추가
            existingList.add(journal)
        }

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
        prefs.edit().remove(getFolderKey(folderId)).apply()
    }

    fun saveAllJournals(context: Context, folderId: Long, journals: List<JournalSummaryDto>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(journals)
        prefs.edit().putString(getFolderKey(folderId), json).apply()
    }

    // 🎯 중복 방지 기능이 완벽 고도화된 추가 메서드
    fun addJournal(context: Context, folderId: Long, newJournal: JournalSummaryDto) {
        val existingList = getJournals(context, folderId).toMutableList()

        if (existingList.none { it.id == newJournal.id }) {
            existingList.add(newJournal)
            saveAllJournals(context, folderId, existingList)
        } else {
            // 혹시라도 중복 인입되면 수정으로 안전 원복 우회 처리
            saveJournal(context, folderId, newJournal)
        }
    }
}