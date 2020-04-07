package com.yakirarie.chatapp

import android.content.Intent
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_row.view.*


class ChatFromItem(val chatMessage: ChatMessage, val user: User) :
    Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
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
        if (chatMessage.toId.size == 1)
            checkIfMessageHasBeenSeen(viewHolder)

        viewHolder.itemView.textViewFromRow.text = chatMessage.text
        val dateAndTime = chatMessage.dateAndTime.split(" ")
        viewHolder.itemView.timestampFromRow.text = "${dateAndTime[1]}\n${dateAndTime[0]}"
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.imageViewFromRow)

        viewHolder.itemView.imageViewFromRow.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
            intent.putExtra("image_url", user.profileImageUrl)
            intent.putExtra("media_type", "image")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }


    private fun handleImageMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.sendImageFromRow.visibility = View.VISIBLE
        viewHolder.itemView.frameLayoutFromRow.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonFromRow.visibility = View.GONE
        viewHolder.itemView.sendVideoFromRowThumbnail.visibility = View.GONE
        viewHolder.itemView.textViewFromRow.visibility = View.GONE

        Glide.with(viewHolder.itemView.context).load(chatMessage.text)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendImageFromRow)

        viewHolder.itemView.sendImageFromRow.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
            intent.putExtra("image_url", chatMessage.text)
            intent.putExtra("media_type", chatMessage.messageType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }

    private fun handleVideoMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.frameLayoutFromRow.visibility = View.VISIBLE
        viewHolder.itemView.videoPreviewPlayButtonFromRow.visibility = View.VISIBLE
        viewHolder.itemView.sendVideoFromRowThumbnail.visibility = View.VISIBLE
        viewHolder.itemView.sendImageFromRow.visibility = View.GONE
        viewHolder.itemView.textViewFromRow.visibility = View.GONE


        Glide.with(viewHolder.itemView.context).load(chatMessage.text).thumbnail(0.1f)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendVideoFromRowThumbnail)


        viewHolder.itemView.videoPreviewPlayButtonFromRow.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
            intent.putExtra("image_url", chatMessage.text)
            intent.putExtra("media_type", chatMessage.messageType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }

    private fun handleTextMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.frameLayoutFromRow.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonFromRow.visibility = View.GONE
        viewHolder.itemView.sendVideoFromRowThumbnail.visibility = View.GONE
        viewHolder.itemView.sendImageFromRow.visibility = View.GONE
        viewHolder.itemView.textViewFromRow.visibility = View.VISIBLE
        viewHolder.itemView.textViewFromRow.text = chatMessage.text
    }

    private fun checkIfMessageHasBeenSeen(viewHolder: GroupieViewHolder){
        if (chatMessage.seen) {
            viewHolder.itemView.messageSeen.visibility = View.VISIBLE
            viewHolder.itemView.messageNotSeen.visibility = View.GONE

        } else {
            viewHolder.itemView.messageSeen.visibility = View.GONE
            viewHolder.itemView.messageNotSeen.visibility = View.VISIBLE

        }
    }

}