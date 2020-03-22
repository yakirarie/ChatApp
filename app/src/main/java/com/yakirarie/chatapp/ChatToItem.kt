package com.yakirarie.chatapp

import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatToItem : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }
}