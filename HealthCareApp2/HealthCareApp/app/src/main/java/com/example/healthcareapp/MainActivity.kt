package com.example.healthcareapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import com.example.healthcareapp.LoadingActivity

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_login2)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("273956393814-8q7knfjqnrrlnvihtgbbucj2rar9uojg.apps.googleusercontent.com") // Firebase 연동 시 자동 생성되는 ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("GoogleLogin", "resultCode: ${result.resultCode}, RESULT_OK=$RESULT_OK")
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("GoogleLogin", "계정 획득 성공: ${account.email}")
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "구글 로그인 실패 statusCode: ${e.statusCode}, message: ${e.message}")
                    Toast.makeText(this, "로그인 실패: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("GoogleLogin", "RESULT_OK 아님. resultCode=${result.resultCode}")
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    task.getResult(ApiException::class.java)
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "실패 statusCode: ${e.statusCode}")
                }
            }
        }



        val loginButton = findViewById<Button>(R.id.btn_google_login)
        loginButton.setOnClickListener {
            signIn()
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
                    val name = user?.displayName ?: ""

                    // 1. SharedPreferences에 이메일 영구 저장
                    saveUserEmail(email)

                    Log.d("FirebaseAuth", "로그인 성공: $email")

                    // 2. 다음 화면으로 이동
                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("ID_TOKEN", idToken)
                    intent.putExtra("USER_EMAIL", email)
                    intent.putExtra("USER_NAME", name)
                    startActivity(intent)
                    finish() // 로그인 완료 후 메인으로 못 돌아오게 종료
                } else {
                    Log.e("FirebaseAuth", "인증 실패: ${task.exception?.message}")
                    Toast.makeText(this, "인증 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 이메일 저장 함수
    private fun saveUserEmail(email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_EMAIL", email)
            apply() // 비동기로 데이터 저장
        }
    }
}

