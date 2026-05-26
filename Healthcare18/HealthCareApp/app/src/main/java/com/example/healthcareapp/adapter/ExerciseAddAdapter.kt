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

    // 현재 체크된 아이템들을 담는 셋
    private val selectedItems = mutableSetOf<ExerciseItem>()

    inner class ViewHolder(val binding: ItemExersiseAddBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExersiseAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val currentPos = holder.bindingAdapterPosition
        if (currentPos == RecyclerView.NO_POSITION) return

        holder.binding.apply {
            // 1. 운동 이름 매핑
            tvExerciseName.text = item.name

            // 2. 체크박스 상태 초기화 (무한 루프 방지를 위해 리스너를 잠시 끕니다)
            cbExercise.setOnCheckedChangeListener(null)
            cbExercise.isChecked = selectedItems.any { it.id == item.id }

            // 3. 체크박스 상태 변경 리스너 (기존 정밀 ID 제거 로직 보존)
            cbExercise.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // 중복 추가 방지 가드링
                    if (selectedItems.none { it.id == item.id }) {
                        selectedItems.add(item)
                    }
                } else {
                    selectedItems.removeAll { it.id == item.id }
                }
                onSelectionChanged(selectedItems.toList())
            }

            // 4. 별표(즐겨찾기) 클릭 리스너 (중복 제거 후 하나로 통합)
            btnFavorite.setOnClickListener {
                btnFavorite.isSelected = !btnFavorite.isSelected
                // 💡 추후 로컬 DB나 서버에 즐겨찾기 상태를 동기화하려면 여기에 코드를 추가하세요.
            }

            // 5. 아이템 항목 전체 클릭 시 체크박스 토글 (사용자 경험 개선)
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
    // 이 함수를 추가하여 Activity에서 호출할 수 있게 합니다.
    fun getSelectedItems(): MutableSet<ExerciseItem> {
        return selectedItems
    }


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