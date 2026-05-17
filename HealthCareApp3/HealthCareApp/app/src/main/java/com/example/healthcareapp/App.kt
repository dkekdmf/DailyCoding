package com.example.healthcareapp

import android.app.Application
import android.content.Intent
import co.ab180.airbridge.Airbridge
import co.ab180.airbridge.AirbridgeOptionBuilder
import com.example.healthcareapp.BuildConfig
import com.example.healthcareapp.network.RetrofitClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val option = AirbridgeOptionBuilder("ttdev", BuildConfig.AIRBRIDGE_APP_TOKEN).build()
        Airbridge.initializeSDK(this, option)

        // 앱 미설치 상태에서 초대 링크 클릭 후 설치된 경우 처리
        Airbridge.handleDeferredDeeplink { uri ->
            val intent = Intent(this, InviteAcceptActivity::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        RetrofitClient.tokenProvider = {
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("access_token", null)
        }
    }
}
