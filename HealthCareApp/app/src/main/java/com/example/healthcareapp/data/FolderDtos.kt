package com.example.healthcareapp.data

data class InviteLinkResponse(
    val inviteLink: String
)

data class InviteAcceptRequest(
    val folderId: Long,
    val token: String
)

data class FolderMemberResponse(
    val userId: Long,
    val nickname: String
)

data class FolderResponse(
    val folderId: Long,
    val name: String,
    val isShared: Boolean,
    val memberCount: Int,
    val members: List<FolderMemberResponse>,
    val createdAt: String?,
    val updatedAt: String?
)

data class FolderListResponse(
    val folders: List<FolderResponse>,
    val nextCursor: String?,
    val hasNext: Boolean
)

data class CreateFolderRequest(val name: String)

data class CreateFolderResponse(
    val folderId: Long,
    val name: String,
    val isShared: Boolean,
    val createdAt: String?
)

data class UpdateFolderRequest(val name: String)

data class UpdateFolderResponse(
    val folderId: Long,
    val name: String,
    val isShared: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
