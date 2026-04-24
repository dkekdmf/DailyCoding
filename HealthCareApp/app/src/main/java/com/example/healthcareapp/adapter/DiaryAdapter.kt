package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.DiaryItem

class DiaryAdapter(private val items: List<DiaryItem>, private val onItemClick: (DiaryItem)->Unit) :
    RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvDate : TextView = view.findViewById(R.id.tv_date)
        val tvTitle : TextView = view.findViewById(R.id.tv_title)
        val tvAuthor : TextView = view.findViewById(R.id.tv_author)

        fun bind(item : DiaryItem){
            tvDate.text = item.date
            tvTitle.text = item.title
            tvAuthor.text = item.author
            itemView.setOnClickListener{
                onItemClick(item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size


}

