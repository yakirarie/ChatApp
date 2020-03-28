package com.yakirarie.chatapp

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
        Log.d("bdika", "bdika "+chatMessage.image)
        if (chatMessage.image) {
            viewHolder.itemView.sendImageFromRow.visibility = View.VISIBLE
            viewHolder.itemView.textViewFromRow.visibility = View.GONE
            Glide.with(viewHolder.itemView.context).load(chatMessage.text).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendImageFromRow)
        } else
            viewHolder.itemView.textViewFromRow.text = chatMessage.text

        val timeAndDate = chatMessage.timestamp.split(" ")
        viewHolder.itemView.timestampFromRow.text = "${timeAndDate[1]}\n${timeAndDate[0]}"
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl).diskCacheStrategy(
            DiskCacheStrategy.ALL
        ).into(viewHolder.itemView.imageViewFromRow)
    }
}