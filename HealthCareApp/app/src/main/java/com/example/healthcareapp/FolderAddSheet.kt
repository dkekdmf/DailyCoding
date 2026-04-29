package com.example.healthcareapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment


class FolderAddConfirmSheet(
    val onConfirm: () -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return inflater.inflate(R.layout.add_confrim_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            dismiss()
        }


        view.findViewById<Button>(R.id.btn_submit_add).setOnClickListener {
            onConfirm()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            // dp를 pixel로 변환하는 공식 (339dp, 271dp 고정)
            val widthPx = (339 * resources.displayMetrics.density).toInt() // 너비 높이 고정
            val heightPx = (271 * resources.displayMetrics.density).toInt()

            dialog.window?.apply {

                setLayout(widthPx, heightPx)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


                val params = attributes
                params.gravity = android.view.Gravity.CENTER
                attributes = params
            }
        }
    }
}