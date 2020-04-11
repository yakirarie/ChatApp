package com.yakirarie.chatapp.viewPagerAdapters

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.fragments.SliderFragment

class SliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val slidesImages =
        listOf(
            R.drawable.logo_slider,
            R.drawable.group_slider,
            R.drawable.chat_slider
        )
    private val slidesHeads = listOf("ABOUT", "CHAT", "ALL SET")
    private val slidesDesc = listOf(
        "Enjoy this newly chat application created by Yakir Arie",
        "Use it to communicate between other users, and even start a group chat!",
        "So what are you waiting for? Let's get Started!"
    )

    override fun getItemCount(): Int {
        return slidesHeads.size
    }

    override fun createFragment(position: Int): Fragment {
        return SliderFragment(slidesImages[position], slidesHeads[position], slidesDesc[position])
    }


}

