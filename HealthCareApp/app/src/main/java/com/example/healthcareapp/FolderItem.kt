package com.example.healthcareapp

data class FolderItem(
    var name: String,
    var lastmodified: String,
    var status: String,
    var isShared: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)