package com.example.healthcareapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.example.healthcareapp.fragment.DiaryListFragment
import com.example.healthcareapp.fragment.FolderMainFragment
import com.example.healthcareapp.fragment.MyPageFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var tabFolder: LinearLayout
    private lateinit var tabJournal: LinearLayout
    private lateinit var tabMy: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navhome)

        tabFolder = findViewById(R.id.tab_folder)
        tabJournal = findViewById(R.id.tab_journal)
        tabMy = findViewById(R.id.tab_my)

        if (savedInstanceState == null) {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        tabFolder.setOnClickListener {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        tabJournal.setOnClickListener {
            replaceFragment(DiaryListFragment())
            updateTabUI(tabJournal)
        }

        tabMy.setOnClickListener {
            replaceFragment(MyPageFragment())
            updateTabUI(tabMy)
        }
    }

    fun moveToJournalTab(folderId: Long, folderName: String, isSharedMode: Boolean) {
        val journalFragment = DiaryListFragment().apply {
            arguments = Bundle().apply {
                putLong("FOLDER_ID", folderId)
                putString("FOLDER_NAME", folderName)
                putBoolean("IS_SHARED_MODE", isSharedMode)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, journalFragment)
            .addToBackStack(null)
            .commit()

        updateTabUI(tabJournal)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_container, fragment)
            .commit()
    }

    private fun updateTabUI(selectedTab: LinearLayout) {
        val tabs = listOf(tabFolder, tabJournal, tabMy)

        tabs.forEach { tab ->
            val isSelected = (tab == selectedTab)

            tab.setBackgroundResource(if (isSelected) R.drawable.tab_selected else 0)

            val icon = tab.getChildAt(0) as? ImageView
            val text = tab.getChildAt(1) as? TextView

            icon?.let {
                val color = if (isSelected) Color.BLACK else Color.parseColor("#AAAAAA")
                it.imageTintList = ColorStateList.valueOf(color)
            }

            text?.let {
                it.setTextColor(if (isSelected) Color.BLACK else Color.parseColor("#AAAAAA"))
            }
        }
    }
}