package com.yakirarie.chatapp

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatToItem(val text: String, val timestamp: String, val user: User) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewToRow.text = text
        val timeAndDate = timestamp.split(" ")
        viewHolder.itemView.timestampToRow.text = "${timeAndDate[1]} ${timeAndDate[0]}"
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl).diskCacheStrategy(
            DiskCacheStrategy.ALL).into(viewHolder.itemView.imageViewToRow)


    }
}