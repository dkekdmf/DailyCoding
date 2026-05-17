package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.GoogleLoginRequest
import com.example.healthcareapp.data.UserResponse
import com.example.healthcareapp.network.RetrofitClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signuploading)

        val loadingimg = findViewById<ImageView>(R.id.iv_loading)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)

        loadingimg.startAnimation(rotateAnimation)



        val idToken = intent.getStringExtra("ID_TOKEN") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val nickname = intent.getStringExtra("USER_NAME") ?: ""
        Log.d("LoadingActivity", "전달받은 토큰: $idToken")

        sendTokenToServer(idToken, email, nickname)
    }

    private fun sendTokenToServer(idToken: String, email: String, nickname: String) {
        val request = GoogleLoginRequest(idToken, email, nickname)

        RetrofitClient.authService.RegisterUser(request).enqueue(object : Callback<ApiResponse<UserResponse>> {
            override fun onResponse(call: Call<ApiResponse<UserResponse>>, response: Response<ApiResponse<UserResponse>>) {
                MainScope().launch {
                    if (response.isSuccessful) {
                        val userResponse = response.body()?.data
                        userResponse?.accessToken?.let { token ->
                            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
                                .putString("access_token", token)
                                .putLong("user_id", userResponse.userId ?: -1L)
                                .apply()
                        }
                        delay(2200)
                        startActivity(Intent(this@LoadingActivity, LoginSuccess::class.java))
                        finish()
                    } else {
                        Log.e("LoadingActivity", "응답 코드: ${response.code()}")
                        Log.e("LoadingActivity", "에러 바디: ${response.errorBody()?.string()}")
                        Toast.makeText(this@LoadingActivity, "서버 거절", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserResponse>>, t: Throwable) {
                Log.e("LoadingActivity", "네트워크 에러: ${t.message}")
                Toast.makeText(this@LoadingActivity, "네트워크 연결 실패", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
}
