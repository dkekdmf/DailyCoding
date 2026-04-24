//package com.example.healthcareapp.adapter
//
//import androidx.fragment.app.Fragment
//import androidx.viewpager2.adapter.FragmentStateAdapter
//import com.example.healthcareapp.FolderFragment
//import com.example.healthcareapp.PrivateFolderFragment
//
//class FolderPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
//    override fun getItemCount(): Int =2
//
//    override fun createFragment(position: Int): Fragment {
//        return when (position){
//            0->FolderFragment()
//            else -> PrivateFolderFragment()
//        }
//    }
//
//
//}