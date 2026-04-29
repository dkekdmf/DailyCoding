package com.example.healthcareapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.ExerciseItem
import com.example.healthcareapp.databinding.ItemExersiseAddBinding

class ExerciseAddAdapter(
    private var items: List<ExerciseItem>,
    private val onSelectionChanged: (List<ExerciseItem>) -> Unit
) : RecyclerView.Adapter<ExerciseAddAdapter.ViewHolder>() {

    // 현재 체크된 아이템들을 담는 셋 (중복 방지 및 빠른 검색)
    private val selectedItems = mutableSetOf<ExerciseItem>()

    inner class ViewHolder(val binding: ItemExersiseAddBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExersiseAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            // 1. 기본 텍스트 설정
            tvExerciseName.text = item.name
            btnFavorite.setOnClickListener {
                it.isSelected = !it.isSelected

            }

            cbExercise.setOnCheckedChangeListener(null)
            cbExercise.isChecked = selectedItems.any { it.id == item.id }

            // 3. 체크박스 클릭 리스너
            cbExercise.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(item)
                } else {
                    // ID를 기준으로 정확하게 제거
                    selectedItems.removeAll { it.id == item.id }
                }
                onSelectionChanged(selectedItems.toList())
            }

            // 4. 별표(즐겨찾기) 클릭 리스너 (요구사항 반영)
            btnFavorite.setOnClickListener {
                // 즐겨찾기 로직이 필요하다면 여기에 작성 (현재는 클릭 시 시각적 효과만)
                btnFavorite.isSelected = !btnFavorite.isSelected
            }

            // 5. 아이템 자체를 눌러도 체크박스가 토글되게 하면 사용자 경험이 좋아집니다.
            root.setOnClickListener {
                cbExercise.isChecked = !cbExercise.isChecked
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 필터링된 리스트로 갱신
    fun updateList(newList: List<ExerciseItem>) {
        items = newList
        notifyDataSetChanged()
    }

    /**
     * 상단 칩의 X를 눌렀을 때 Activity에서 호출하는 함수
     */
    fun removeSelection(exerciseId: String) {
        // ID가 일치하는 아이템을 찾아서 삭제
        val removed = selectedItems.removeIf { it.id == exerciseId }

        if (removed) {
            // UI에 체크 해제 반영
            notifyDataSetChanged()
            // 데이터 변경 알림
            onSelectionChanged(selectedItems.toList())
        }
    }
}