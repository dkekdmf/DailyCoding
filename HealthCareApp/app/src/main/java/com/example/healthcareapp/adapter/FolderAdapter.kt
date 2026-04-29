package com.example.healthcareapp.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.DiaryActivity
import com.example.healthcareapp.DiaryListActivity
import com.example.healthcareapp.FolderItem
import com.example.healthcareapp.FolderItem2
import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R
import java.util.Random


class FolderAdapter(
    var items: MutableList<FolderItem>,    // 폴더 데이터 리스트
    val onMoreClick: (FolderItem) -> Unit // 더보기(...) 버튼 클릭 시 호출할 함수 (바텀시트 연결)
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    // 아이템 왼쪽 뷰에 적용 색상 리스트 (피그마 디자인팀)
    private val stripeColors = intArrayOf(
        Color.parseColor("#FF6969"), Color.parseColor("#FF9245"),
        Color.parseColor("#FFD153"), Color.parseColor("#94A769"),
        Color.parseColor("#5DCE46"), Color.parseColor("#83F2FA"),
        Color.parseColor("#53A1FF"), Color.parseColor("#8A38F5"),
        Color.parseColor("#FF5DEF"), Color.parseColor("#A39288")
    )

    // 레이아웃 내의 개별 뷰들
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)       // 폴더 제목
        val tvStatus: TextView = view.findViewById(R.id.tv_status)           // 공유 상태 텍스트
        val btnMore: View = view.findViewById(R.id.btn_more)                 // 더보기 버튼
        val leftStripe: View = view.findViewById(R.id.view_left_stripe)       // 왼쪽 컬러 띠
        val lastmodified: TextView = view.findViewById(R.id.tv_last_modified) // 수정 시간
        val layoutstatus : ConstraintLayout = view.findViewById(R.id.layout_status_badge_container) // 상태 뱃지 배경
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // folder_item3 레이아웃을 객체화하여 뷰홀더에 담아 반환
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 1. 현재 포지션의 데이터 객체 가져오기
        val folder = items[position]

        // 2. 기본 데이터 세팅 (폴더명, 수정일)
        holder.tvTitle.text = folder.name
        holder.lastmodified.text = "${folder.lastmodified}에 최종 수정"

        // 3. 고유 색상 띠 설정
        val color = stripeColors[position % stripeColors.size]
        holder.leftStripe.setBackgroundColor(color)

        // 4. 초기 공유 상태 UI 설정
        updateStatusUI(holder, folder.isShared)

        // 5. 공유 상태 뱃지 클릭 시 처리 (이벤트 전파 방지 확인)
        holder.tvStatus.setOnClickListener {
            folder.isShared = !folder.isShared
            updateStatusUI(holder, folder.isShared)
            // 팁: 여기서 상태 변경 후 서버에 업데이트 API를 날리면 좋습니다.
        }

        // 6. 더보기 버튼 클릭 시 처리
        holder.btnMore.setOnClickListener {
            onMoreClick(folder)
        }

        // 7. [핵심] 폴더 전체(itemView) 클릭 시 화면 이동
        holder.itemView.setOnClickListener {
            val context = it.context
            // context가 HomeActivity인지 확인 후 함수 호출
            if (context is HomeActivity) {
                context.moveToJournalTab(folder.folderId, folder.name)
            }
        }
    }

    // 공유 여부에 따라 뱃지의 배경색과 글자색을 변경
    private fun updateStatusUI(holder: ViewHolder, isShared: Boolean) {
        if (isShared) {
            // 공유 중일 때: 파란색 테마
            holder.tvStatus.text = "공유중"
            holder.layoutstatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E1EFFF")) // 연한 파랑 배경
            holder.tvStatus.setTextColor(Color.parseColor("#3A8DFF")) // 진한 파랑 글자
        } else {
            // 공유 대기일 때: 회색 테마
            holder.tvStatus.text = "공유대기"
            holder.layoutstatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F2F2F2")) // 연한 회색 배경
            holder.tvStatus.setTextColor(Color.parseColor("#888888")) // 진한 회색 글자
        }
    }

    override fun getItemCount() = items.size

  // 생성일순, 최근사용순 리스트 갱신
    fun updateData(newItems: List<FolderItem>) {
        this.items.clear()          // 기존 데이터 삭제
        this.items.addAll(newItems) // 새 데이터 추가
        notifyDataSetChanged()      // 리스트 전체 다시 그리기
    }
}