package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InviteAcceptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_accept)

        // 시연용 텍스트 가이드 레이아웃 노출
        findViewById<TextView>(R.id.tv_invite_message).text = "초대 링크를 통해 폴더 공유에 참여합니다."

        findViewById<Button>(R.id.btn_accept).setOnClickListener {
            // 🎬 이제 여기서 ID를 하드코딩해서 굽지 않습니다! (이미 프래그먼트에서 진짜 ID로 구워졌기 때문)
            // 깔끔하게 메인 홈 화면으로 트랜지션 이동시킵니다.
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btn_decline).setOnClickListener {
            finish()
        }
    }
}