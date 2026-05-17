package com.example.healthcareapp.network

import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.CreateFolderRequest
import com.example.healthcareapp.data.CreateFolderResponse
import com.example.healthcareapp.data.FolderListResponse
import com.example.healthcareapp.data.FolderResponse
import com.example.healthcareapp.data.InviteAcceptRequest
import com.example.healthcareapp.data.InviteLinkResponse
import com.example.healthcareapp.data.UpdateFolderRequest
import com.example.healthcareapp.data.UpdateFolderResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DiaryFolderApiService {

    @GET("/api/folders")
    fun getFolders(
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "UPDATED_AT",
        @Query("cursor") cursor: String? = null
    ): Call<ApiResponse<FolderListResponse>>

    @POST("/api/folders")
    fun createFolder(@Body request: CreateFolderRequest): Call<ApiResponse<CreateFolderResponse>>

    @PATCH("/api/folders/{folderId}")
    fun updateFolderName(
        @Path("folderId") folderId: Long,
        @Body request: UpdateFolderRequest
    ): Call<ApiResponse<UpdateFolderResponse>>

    @DELETE("/api/folders/{folderId}/members/me")
    fun leaveFolder(@Path("folderId") folderId: Long): Call<Void>

    @POST("/api/folders/{folderId}/invite-link")
    fun createInviteLink(@Path("folderId") folderId: Long): Call<ApiResponse<InviteLinkResponse>>

    @POST("/api/folders/invite/accept")
    fun acceptInvite(@Body request: InviteAcceptRequest): Call<ApiResponse<FolderResponse>>
}
