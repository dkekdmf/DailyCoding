package com.example.healthcareapp.data

data class FolderItem2(
    var name: String, // 이름
    var lastmodified: String, //최종 수정일
    var status: String,
    var isShared: Boolean = false, // 공유
    val createdAt: Long = System.currentTimeMillis() // 작성일
)