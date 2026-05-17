package com.example.healthcareapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_login2)

        setupPolicyTextView()

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("273956393814-8q7knfjqnrrlnvihtgbbucj2rar9uojg.apps.googleusercontent.com") // Firebase 연동 시 자동 생성되는 ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "로그인 실패: ${e.message}")
                }
            }
        }

        findViewById<Button>(R.id.btn_google_login).setOnClickListener {
            signIn()
        }
    }

    /**
     * 이용약관 및 개인정보처리방침 텍스트 스타일 및 클릭 리스너 설정
     */
    private fun setupPolicyTextView() {
        val policyTextView = findViewById<TextView>(R.id.text_policy)
        val fullText = "계속하면 이용약관 및\n개인정보처리방침에 동의하게 됩니다"
        val spannableString = SpannableString(fullText)

        // 1. [이용약관] 클릭 및 스타일 설정
        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // 이용약관 링크 연결
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mint-kip-89b.notion.site/33ef8c8d299980799276dd435fe87b70"))
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#94A3B8") // 이미지의 회색 유지
                ds.isUnderlineText = true             // 밑줄 유지
            }
        }
        val termStart = fullText.indexOf("이용약관")
        spannableString.setSpan(termsClickableSpan, termStart, termStart + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 2. [개인정보처리방침] 클릭 및 스타일 설정
        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // 개인정보처리방침 링크 연결
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mint-kip-89b.notion.site/359f8c8d299980dc9e02cbc830185052"))
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#94A3B8")
                ds.isUnderlineText = true
            }
        }
        val privacyStart = fullText.indexOf("개인정보처리방침")
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyStart + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TextView 반영
        policyTextView.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance() //클릭 활성화를 위해 필수
            highlightColor = Color.TRANSPARENT               // 클릭 시 배경색 제거
            gravity = Gravity.CENTER
        }
    }

    private fun signIn() {
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val email = user?.email ?: ""
                    saveUserEmail(email)
                    val intent = Intent(this, LoadingActivity::class.java).apply {
                        putExtra("USER_EMAIL", email)
                        putExtra("USER_NAME", user?.displayName ?: "")
                    }
                    startActivity(intent)
                    finish()
                }
            }
    }

    private fun saveUserEmail(email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("USER_EMAIL", email).apply()
    }
}