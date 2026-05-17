package com.example.healthcareapp.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.healthcareapp.R

/**
 * 마이페이지 프래그먼트: 사용자의 계정 정보(이메일 등)를 확인하고 관리하는 화면
 */
class MyPageFragment : Fragment() {

    /**
     * 프래그먼트의 레이아웃(XML)을 인플레이트하여 뷰를 생성함
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_mypage.xml 레이아웃을 기반으로 뷰 생성
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        // 1. [데이터 로드] SharedPreferences에서 로컬에 저장된 사용자 정보(이메일)를 불러옴
        // "UserPrefs"라는 이름의 저장소에서 "USER_EMAIL" 키 값을 가져오며, 없을 경우 기본값 전달
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "이메일 정보 없음")

        // 2. [UI 반영] 화면에 있는 이메일 표시용 TextView를 찾아 데이터를 설정함
        val getEmail = view.findViewById<TextView>(R.id.signedupemail)
        getEmail.text = userEmail

        return view
    }

    /**
     * 팁: 만약 로그아웃이나 회원탈퇴 기능을 추가한다면 여기서 SharedPreferences를
     * clear() 해주는 로직을 작성하면 됩니다.
     */
}