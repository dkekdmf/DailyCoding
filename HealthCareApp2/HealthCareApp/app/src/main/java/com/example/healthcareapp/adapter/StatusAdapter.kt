package com.example.healthcareapp // 재훈님의 패키지명에 맞춰 수정하세요

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter(private val questions: List<String>) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tv_question_index)
        val tvTitle: TextView = view.findViewById(R.id.tv_question_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_condition_question, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 첫 번째 질문이 밖에 따로 있으므로, 리스트는 2번부터 시작하게 표시합니다.
        holder.tvNumber.text = "${position + 2}/5"
        holder.tvTitle.text = questions[position]
    }

    override fun getItemCount(): Int = questions.size
}