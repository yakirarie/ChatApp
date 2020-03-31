package com.yakirarie.chatapp

import android.content.Intent
import android.net.Uri
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
        if (chatMessage.messageType == "image") {
            handleImageMessage(viewHolder)
        } else if (chatMessage.messageType == "video") {
            handleVideoMessage(viewHolder)
        } else {
            handleTextMessage(viewHolder)
        }

        val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId[0]
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

                viewHolder.itemView.imageViewLatestProfile.setOnClickListener {
                    val intent = Intent(viewHolder.itemView.context, FullScreenMedia::class.java)
                    intent.putExtra("image_url", chatPartnerUser?.profileImageUrl)
                    intent.putExtra("media_type", "image")

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    viewHolder.itemView.context.startActivity(intent)
                }

            }

        })
    }

    private fun handleImageMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.sendImageLatestMessage.visibility = View.VISIBLE
        viewHolder.itemView.sendVideoLatestMessageThumbnail.visibility = View.GONE
        viewHolder.itemView.frameLayoutLatestMessage.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonLatestMessage.visibility = View.GONE
        viewHolder.itemView.textViewLatestMessage.visibility = View.GONE
        Glide.with(viewHolder.itemView.context).load(chatMessage.text)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendImageLatestMessage)
    }

    private fun handleVideoMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.sendVideoLatestMessageThumbnail.visibility = View.VISIBLE
        viewHolder.itemView.frameLayoutLatestMessage.visibility = View.VISIBLE
        viewHolder.itemView.videoPreviewPlayButtonLatestMessage.visibility = View.VISIBLE
        viewHolder.itemView.sendImageLatestMessage.visibility = View.GONE
        viewHolder.itemView.textViewLatestMessage.visibility = View.GONE

        Glide.with(viewHolder.itemView.context).load(chatMessage.text)
            .thumbnail(0.1f)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.sendVideoLatestMessageThumbnail)
    }

    private fun handleTextMessage(viewHolder: GroupieViewHolder){
        viewHolder.itemView.frameLayoutLatestMessage.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonLatestMessage.visibility = View.GONE
        viewHolder.itemView.sendImageLatestMessage.visibility = View.GONE
        viewHolder.itemView.sendVideoLatestMessageThumbnail.visibility = View.GONE
        viewHolder.itemView.textViewLatestMessage.visibility = View.VISIBLE
        viewHolder.itemView.textViewLatestMessage.text = chatMessage.text
    }

}