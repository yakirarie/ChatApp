package com.yakirarie.chatapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_full_screen_image.*

class FullScreenImage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val url = intent.getStringExtra("image_url")
        if (url != null) {
            Glide.with(this).load(url)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
        else{
            val uri = intent.getParcelableExtra<Uri>("image_uri")
            if (uri != null)
                Glide.with(this).load(uri)
                    .placeholder(R.drawable.ic_loading_sign)
                    .error(R.drawable.ic_error_sign)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
        }

    }
}
