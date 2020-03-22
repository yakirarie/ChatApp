package com.yakirarie.chatapp

import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.user_row_message.view.*

class UserItem(val user: User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row_message
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewNewMessageUsername.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageViewNewMessage)

    }
}