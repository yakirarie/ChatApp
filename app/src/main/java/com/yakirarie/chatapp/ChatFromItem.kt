package com.yakirarie.chatapp

import android.content.Intent
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
        if (chatMessage.image) {
            viewHolder.itemView.sendImageFromRow.visibility = View.VISIBLE
            viewHolder.itemView.textViewFromRow.visibility = View.GONE
            Glide.with(viewHolder.itemView.context).load(chatMessage.text)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(viewHolder.itemView.sendImageFromRow)
            viewHolder.itemView.sendImageFromRow.setOnClickListener {
                val intent = Intent(viewHolder.itemView.context, FullScreenImage::class.java)
                intent.putExtra("image_url", chatMessage.text)
                intent.putExtra("user_name",user.username)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                viewHolder.itemView.context.startActivity(intent)
            }
        } else {

            viewHolder.itemView.sendImageFromRow.visibility = View.GONE
            viewHolder.itemView.textViewFromRow.visibility = View.VISIBLE
            viewHolder.itemView.textViewFromRow.text = chatMessage.text

        }

        viewHolder.itemView.textViewFromRow.text = chatMessage.text
        val timeAndDate = chatMessage.timestamp.split(" ")
        viewHolder.itemView.timestampFromRow.text = "${timeAndDate[1]}\n${timeAndDate[0]}"
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.imageViewFromRow)
    }
}