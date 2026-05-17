package com.example.healthcareapp.data

import com.example.zero.healthcare.dto.journal.CompleteJournalRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface JournalService {

    // 🚨 주소와 파라미터 타입을 완벽히 매치해 줍니다.
    @POST("/api/journals/complete")
    fun completeJournal(
        @Body request: CompleteJournalRequest
    ): Call<JournalApiResponse<JournalDetailDto>> // 반환 타입을 JournalDetailDto로 지정!

    // 기존 목록 조회용 메서드
    @GET("/api/journals")
    fun getMyJournals(
        @Query("date") date: String?,
        @Query("from") from: String?,
        @Query("to") to: String?
    ): Call<JournalApiResponse<List<JournalSummaryDto>>>

    @DELETE("/api/journals/{id}")
    fun deleteJournal(
        @Path("id") id: Long
    ): Call<JournalApiResponse<Void>>
}