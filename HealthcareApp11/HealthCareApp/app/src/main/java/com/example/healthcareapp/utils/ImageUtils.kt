package com.example.healthcareapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * 🎯 [인코딩] 갤러리 이미지 URI ➔ SharedPreferences용 Base64 문자열 변환
     * 과도한 고화질 이미지는 Prefs 용량 한계를 초과하여 앱을 무겁게 만들 수 있으므로
     * 샘플링 가공처리를 통해 800x800 규격으로 최적화 압축 변환합니다.
     */
    fun uriToBase64(context: Context, uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            // 1단계: 비트맵 팩토리 옵션을 통한 해상도 최적화 다운샘플링
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 // 이미지 크기를 1/2로 압축하여 로드 (메모리 방어선)
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (bitmap != null) {
                // 2단계: 품질 압축 작업 (용량 경량화)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream) // 70% 압축 품질
                val byteArray = byteArrayOutputStream.toByteArray()

                // 3단계: 최종 Base64 문자열로 굽기
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 🎯 [디코딩] SharedPreferences에 저장된 Base64 문자열 ➔ 이미지뷰용 Bitmap 복원
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            if (base64String.isEmpty()) return null
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}