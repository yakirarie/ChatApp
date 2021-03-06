package com.yakirarie.chatapp.adaptersItems

import android.content.Intent
import android.util.Log
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
import com.yakirarie.chatapp.*
import com.yakirarie.chatapp.activities.FullScreenMediaActivity
import com.yakirarie.chatapp.classObjects.ChatMessage
import com.yakirarie.chatapp.classObjects.Group
import com.yakirarie.chatapp.classObjects.User
import kotlinx.android.synthetic.main.latest_message_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class LatestMessageRow(val chatMessage: ChatMessage, val groupId: String? = null) :
    Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null
    var group: Group? = null
    var enableClick = false

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        when (chatMessage.messageType) {
            "image" -> {
                handleImageMessage(viewHolder)
            }
            "video" -> {
                handleVideoMessage(viewHolder)
            }
            else -> {
                handleTextMessage(viewHolder)
            }
        }

        showTimestamp(viewHolder)
        if (chatMessage.toId.size > 1) { // group msg
            val ref = FirebaseDatabase.getInstance().getReference("/users/$groupId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    group = p0.getValue(Group::class.java)
                    enableClick = true
                    viewHolder.itemView.textViewLatestUsername.text = group?.groupName
                    Glide.with(viewHolder.itemView.context).load(group?.groupImageUrl)
                        .placeholder(R.drawable.ic_loading_sign)
                        .error(R.drawable.ic_error_sign).diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        ).into(viewHolder.itemView.imageViewLatestProfile)

                    viewHolder.itemView.textViewLatestMessage.text =
                        "${group?.usersList?.filter { it.uid == chatMessage.fromId }?.get(0)?.username}: ${chatMessage.text}"
                    Log.d("check", "${viewHolder.itemView.textViewLatestMessage.text}")


                    viewHolder.itemView.imageViewLatestProfile.setOnClickListener {
                        val intent =
                            Intent(viewHolder.itemView.context, FullScreenMediaActivity::class.java)
                        intent.putExtra("image_url", group?.groupImageUrl)
                        intent.putExtra("media_type", "image")

                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        viewHolder.itemView.context.startActivity(intent)
                    }

                }

            })


        } else { // user to user
            checkIfMessageHasBeenSeen(viewHolder)
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
                    enableClick = true

                    viewHolder.itemView.textViewLatestUsername.text = chatPartnerUser?.username
                    Glide.with(viewHolder.itemView.context).load(chatPartnerUser?.profileImageUrl)
                        .placeholder(R.drawable.ic_loading_sign)
                        .error(R.drawable.ic_error_sign).diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        ).into(viewHolder.itemView.imageViewLatestProfile)

                    viewHolder.itemView.imageViewLatestProfile.setOnClickListener {
                        val intent =
                            Intent(viewHolder.itemView.context, FullScreenMediaActivity::class.java)
                        intent.putExtra("image_url", chatPartnerUser?.profileImageUrl)
                        intent.putExtra("media_type", "image")

                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        viewHolder.itemView.context.startActivity(intent)
                    }

                }

            })
        }

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

    private fun handleTextMessage(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.frameLayoutLatestMessage.visibility = View.GONE
        viewHolder.itemView.videoPreviewPlayButtonLatestMessage.visibility = View.GONE
        viewHolder.itemView.sendImageLatestMessage.visibility = View.GONE
        viewHolder.itemView.sendVideoLatestMessageThumbnail.visibility = View.GONE
        viewHolder.itemView.textViewLatestMessage.visibility = View.VISIBLE
        viewHolder.itemView.textViewLatestMessage.text = chatMessage.text

    }

    private fun checkIfMessageHasBeenSeen(viewHolder: GroupieViewHolder) {
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            viewHolder.itemView.latestMessageIsMessageSeen.visibility = View.VISIBLE

            if (chatMessage.seen)
                viewHolder.itemView.latestMessageIsMessageSeen.setImageDrawable(
                    viewHolder.itemView.resources.getDrawable(
                        R.drawable.ic_msg_seen,
                        null
                    )
                )
            else
                viewHolder.itemView.latestMessageIsMessageSeen.setImageDrawable(
                    viewHolder.itemView.resources.getDrawable(
                        R.drawable.ic_msg_not_seen,
                        null
                    )
                )


        } else
            viewHolder.itemView.latestMessageIsMessageSeen.visibility = View.GONE


    }

    private fun showTimestamp(viewHolder: GroupieViewHolder) {
        val currentDay = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Calendar.getInstance().time)
        val dateAndTime = chatMessage.dateAndTime.split(" ")
        if (currentDay == dateAndTime[0])
            viewHolder.itemView.timestampLatestMessage.text = dateAndTime[1]
        else
            viewHolder.itemView.timestampLatestMessage.text = dateAndTime[0]

    }

}