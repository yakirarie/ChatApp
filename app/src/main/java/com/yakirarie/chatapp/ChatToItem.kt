package com.yakirarie.chatapp

import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatToItem(val chatMessage: ChatMessage, val user: User) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        when (chatMessage.messageType) {
            "image" -> {
                handleImageMessage(viewHolder)
            }
            "video" -> {
                handleVideoMessage(viewHolder)
            }
            else -> {
                handleTextMessage(viewHolder)
            }
        }

        val timeAndDate = chatMessage.dateAndTime.split(" ")
        viewHolder.itemView.timestampToRow.text = "${timeAndDate[1]}\n${timeAndDate[0]}"
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.imageViewToRow)

        viewHolder.itemView.imageViewToRow.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
            intent.putExtra("image_url", user.profileImageUrl)
            intent.putExtra("media_type", "image")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }

    }

    private fun handleImageMessage(viewHolder: GroupieViewHolder){
        viewHolder.itemView.sendImageToRow.visibility = View.VISIBLE
        viewHolder.itemView.frameLayoutToRow.visibility = View.GONE
        viewHolder.itemView.sendVideoToRowThumbnail.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonToRow.visibility = View.GONE
        viewHolder.itemView.textViewToRow.visibility = View.GONE

        Glide.with(viewHolder.itemView.context).load(chatMessage.text)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendImageToRow)

        viewHolder.itemView.sendImageToRow.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
            intent.putExtra("image_url", chatMessage.text)
            intent.putExtra("media_type", chatMessage.messageType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }

    private fun handleVideoMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.frameLayoutToRow.visibility = View.VISIBLE
        viewHolder.itemView.videoPreviewPlayButtonToRow.visibility = View.VISIBLE
        viewHolder.itemView.sendVideoToRowThumbnail.visibility = View.VISIBLE
        viewHolder.itemView.sendImageToRow.visibility = View.GONE
        viewHolder.itemView.textViewToRow.visibility = View.GONE

        Glide.with(viewHolder.itemView.context).load(chatMessage.text).thumbnail(0.1f)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendVideoToRowThumbnail)

        viewHolder.itemView.sendVideoToRowThumbnail.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
            intent.putExtra("image_url", chatMessage.text)
            intent.putExtra("media_type", chatMessage.messageType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }

    private fun handleTextMessage(viewHolder: GroupieViewHolder){
        viewHolder.itemView.frameLayoutToRow.visibility = View.GONE
        viewHolder.itemView.sendVideoToRowThumbnail.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonToRow.visibility = View.GONE
        viewHolder.itemView.sendImageToRow.visibility = View.GONE
        viewHolder.itemView.textViewToRow.visibility = View.VISIBLE
        viewHolder.itemView.textViewToRow.text = chatMessage.text
    }

}