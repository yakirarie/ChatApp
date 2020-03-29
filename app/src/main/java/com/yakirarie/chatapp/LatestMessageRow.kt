package com.yakirarie.chatapp

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if (chatMessage.image) {
            viewHolder.itemView.sendImageLatestMessage.visibility = View.VISIBLE
            viewHolder.itemView.textViewLatestMessage.visibility = View.GONE
            Glide.with(viewHolder.itemView.context).load(chatMessage.text)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(viewHolder.itemView.sendImageLatestMessage)

        } else {
            viewHolder.itemView.sendImageLatestMessage.visibility = View.GONE
            viewHolder.itemView.textViewLatestMessage.visibility = View.VISIBLE
            viewHolder.itemView.textViewLatestMessage.text = chatMessage.text
        }

        val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else
            chatMessage.fromId

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.textViewLatestUsername.text = chatPartnerUser?.username
                Glide.with(viewHolder.itemView.context).load(chatPartnerUser?.profileImageUrl)
                    .placeholder(R.drawable.ic_loading_sign)
                    .error(R.drawable.ic_error_sign).diskCacheStrategy(
                        DiskCacheStrategy.ALL
                    ).into(viewHolder.itemView.imageViewLatestProfile)

            }

        })
    }

}