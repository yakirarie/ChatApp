package com.yakirarie.chatapp

import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewToRow.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageViewToRow)


    }
}