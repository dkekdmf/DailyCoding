package com.example.healthcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.ConditionAdapter
import com.example.healthcareapp.data.ConditionRecord

class ConditionCheckFragment : Fragment(R.layout.fragment_condition_check) {
    private lateinit var adapter: ConditionAdapter
    private val conditionList = mutableListOf<ConditionRecord>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv_condition_list)
        adapter = ConditionAdapter(conditionList)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter
    }

    // 종료 버튼 눌렀을 때 호출될 함수
    fun addNewRecord(title: String, time: String) {
        adapter.addItem(ConditionRecord(title, time))
    }
}