package com.yakirarie.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.SliderAdapter
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : FragmentActivity() {
    private lateinit var dots: MutableList<TextView>
    val backButtonsStrings = listOf("", "Back" , "Back")
    val nextButtonString = listOf("Next", "Next", "Finish")
    var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val adapter = SliderAdapter(this)
        slideViewPager.adapter = adapter
        dots = mutableListOf(position1, position2, position3)
        slideViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                currentPosition = position
                buttonNext.text = nextButtonString[position]
                buttonPrev.text = backButtonsStrings[position]
                dots[position].setTextColor(getColor(android.R.color.black))
                dots.forEachIndexed { index, textView ->
                    if (index != position) textView.setTextColor(
                        getColor(android.R.color.white)
                    )
                }
            }

        })

        buttonPrev.setOnClickListener {
            if (buttonPrev.text == "") return@setOnClickListener
            else slideViewPager.currentItem -= 1
        }

        buttonNext.setOnClickListener {
            if (buttonNext.text != "Finish") slideViewPager.currentItem += 1
            else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

    }

}
