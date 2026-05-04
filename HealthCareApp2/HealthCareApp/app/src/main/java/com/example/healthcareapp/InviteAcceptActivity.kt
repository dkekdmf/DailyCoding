package com.example.healthcareapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.ab180.airbridge.Airbridge
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.FolderResponse
import com.example.healthcareapp.data.InviteAcceptRequest
import com.example.healthcareapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InviteAcceptActivity : AppCompatActivity() {

    private var pendingFolderId: Long? = null
    private var pendingToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_accept)

        val handled = Airbridge.handleDeeplink(intent) { uri ->
            processUri(uri)
        }
        if (!handled) {
            processUri(intent?.data)
        }

        findViewById<Button>(R.id.btn_accept).setOnClickListener {
            val folderId = pendingFolderId
            val token = pendingToken
            if (folderId != null && token != null) {
                acceptInvite(folderId, token)
            } else {
                Toast.makeText(this, "유효하지 않은 초대 링크입니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<Button>(R.id.btn_decline).setOnClickListener {
            finish()
        }
    }

    private fun processUri(uri: Uri?) {
        Log.d("InviteAccept", "processUri: $uri")
        if (uri == null) {
            Log.e("InviteAccept", "uri null → finish")
            finish()
            return
        }

        val folderId = uri.getQueryParameter("folderId")?.toLongOrNull()
        val token = uri.getQueryParameter("token")
        val folderName = uri.getQueryParameter("folderName")
        Log.d("InviteAccept", "folderId=$folderId, token=$token, folderName=$folderName")

        if (folderId == null || token.isNullOrBlank()) {
            Log.e("InviteAccept", "folderId 또는 token 없음")
            Toast.makeText(this, "유효하지 않은 초대 링크입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val message = if (!folderName.isNullOrBlank()) "'$folderName' 폴더에 초대되었습니다."
                      else "초대 링크를 통해 폴더에 참여할 수 있습니다."
        findViewById<TextView>(R.id.tv_invite_message).text = message

        val accessToken = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .getString("access_token", null)
        Log.d("InviteAccept", "accessToken=${if (accessToken != null) "있음" else "없음"}")

        if (accessToken == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        pendingFolderId = folderId
        pendingToken = token
    }

    private fun acceptInvite(folderId: Long, token: String) {
        Log.d("InviteAccept", "acceptInvite 호출: folderId=$folderId")
        findViewById<Button>(R.id.btn_accept).isEnabled = false

        RetrofitClient.folderService.acceptInvite(InviteAcceptRequest(folderId, token))
            .enqueue(object : Callback<ApiResponse<FolderResponse>> {
                override fun onResponse(call: Call<ApiResponse<FolderResponse>>, response: Response<ApiResponse<FolderResponse>>) {
                    Log.d("InviteAccept", "응답 코드: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("InviteAccept", "성공 → FolderActivity 이동")
                        Toast.makeText(this@InviteAcceptActivity, "폴더에 참여했습니다!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@InviteAcceptActivity, FolderActivity2::class.java))
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        Log.e("InviteAccept", "실패: code=${response.code()}, body=$errorBody")
                        val msg = when (response.code()) {
                            400 -> when {
                                errorBody.contains("ALREADY_MEMBER") -> "이미 참여 중인 폴더입니다."
                                errorBody.contains("INVALID_TOKEN") -> "유효하지 않은 초대 링크입니다."
                                errorBody.contains("FOLDER_CLOSED") -> "닫힌 폴더입니다."
                                else -> "초대 수락 실패"
                            }
                            409 -> "이미 2명이 참여 중입니다."
                            else -> "초대 수락 실패 (${response.code()})"
                        }
                        Toast.makeText(this@InviteAcceptActivity, msg, Toast.LENGTH_SHORT).show()
                        findViewById<Button>(R.id.btn_accept).isEnabled = true
                    }
                }

                override fun onFailure(call: Call<ApiResponse<FolderResponse>>, t: Throwable) {
                    Log.e("InviteAccept", "네트워크 실패: ${t.message}")
                    Toast.makeText(this@InviteAcceptActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    findViewById<Button>(R.id.btn_accept).isEnabled = true
                }
            })
    }
}
