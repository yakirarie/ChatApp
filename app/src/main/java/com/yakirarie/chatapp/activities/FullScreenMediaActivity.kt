package com.yakirarie.chatapp.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yakirarie.chatapp.R
import kotlinx.android.synthetic.main.activity_full_screen_media.*


class FullScreenMediaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_media)

        val url = intent.getStringExtra("image_url")
        val mediaType= intent.getStringExtra("media_type")

        if (url != null) {
            if (mediaType == "image") {
                imageView.visibility = View.VISIBLE
                videoView.visibility = View.INVISIBLE
                loading_image.visibility = View.INVISIBLE

                Glide.with(this).load(url)
                    .placeholder(R.drawable.ic_loading_sign)
                    .error(R.drawable.ic_error_sign)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
            else if (mediaType == "video"){
                videoView.visibility = View.VISIBLE
                imageView.visibility = View.INVISIBLE
                videoView.setVideoURI(Uri.parse(url))
                videoView.setOnPreparedListener{
                    loading_image.visibility = View.GONE
                    it.seekTo(1)
                }
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)
                mediaController.setAnchorView(videoView)


            }
        }
        else{
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.INVISIBLE
            loading_image.visibility = View.INVISIBLE

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
