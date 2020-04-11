package com.yakirarie.chatapp.viewPagerAdapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yakirarie.chatapp.fragments.SliderFragment

class HomeAdapter(aca: AppCompatActivity, val fragments: List<Fragment>) : FragmentStateAdapter(aca) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }


}

