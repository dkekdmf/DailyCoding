package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding
import com.example.healthcareapp.widget.Slidercustom

class StatusQuestionAdapter(private val questions: List<StatusQuestion>) :
    RecyclerView.Adapter<StatusQuestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]

        holder.binding.apply {
            tvStepCount.text = "${item.id}/5"
            tvQuestionTitle.text = item.title
            tvMinLabel.text = item.minLabel
            tvMaxLabel.text = item.maxLabel

            // 🎯 SeekBar 대신 Slidercustom 사용
            val dotSlider = root.findViewById<Slidercustom>(R.id.slider)
            dotSlider.max = 10

            // 초기값 설정 및 가이드 텍스트 업데이트
            dotSlider.progress = item.score
            tvSliderGuide.text = item.guides[item.score] ?: "${item.score} - 선택됨"

            // 🎯 값 변경 시 로직 (리스너 간소화)
            dotSlider.onProgressChanged = { value ->
                val finalScore = if (value < 1) 1 else value
                item.score = finalScore
                tvSliderGuide.text = item.guides[finalScore] ?: "$finalScore - 선택됨"
            }
        }
    }

    override fun getItemCount(): Int = questions.size
}