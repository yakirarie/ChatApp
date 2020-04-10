package com.yakirarie.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yakirarie.chatapp.R
import kotlinx.android.synthetic.main.slide_layout.*

class SliderFragment(val image: Int, val head: String, val desc: String): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.slide_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slideImage.setImageResource(image)
        slideHeading.text = head
        slideDesc.text = desc

    }
}