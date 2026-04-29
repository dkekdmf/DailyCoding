package com.example.healthcareapp

data class FolderItem(
    var folderId: Long = 0L,
    var name: String,
    var isShared: Boolean,
    var lastmodified: String
)