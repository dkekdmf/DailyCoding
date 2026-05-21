package com.example.healthcareapp.utils

import com.example.healthcareapp.R

object ColorUtils {
    private val emojiResources = intArrayOf(
        R.drawable.emoticon1, R.drawable.emoticon2,
        R.drawable.emoticon3, R.drawable.emoticon4, R.drawable.emoticon5
    )

    // 🎯 id를 인자로 추가하여 일지마다 고유한 키 생성
    fun getStableEmojiResId(date: String, id: String, folderId: Long): Int {
        val key = "${date}_${id}_$folderId" // 날짜 + 고유ID + 폴더ID 조합
        var hash = 7
        for (ch in key) { hash = hash * 31 + ch.code }
        val index = Math.abs(hash) % emojiResources.size
        return emojiResources[index]
    }
}