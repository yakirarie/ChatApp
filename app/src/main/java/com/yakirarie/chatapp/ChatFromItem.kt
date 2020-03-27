package com.yakirarie.chatapp

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_row.view.*

class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewFromRow.text = text
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl).diskCacheStrategy(
            DiskCacheStrategy.ALL).into(viewHolder.itemView.imageViewFromRow)
    }
}