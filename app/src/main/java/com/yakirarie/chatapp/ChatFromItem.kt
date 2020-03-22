package com.yakirarie.chatapp

import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatFromItem: Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }
}