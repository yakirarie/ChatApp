package com.yakirarie.chatapp.adaptersItems

import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.yakirarie.chatapp.activities.FullScreenMediaActivity
import com.yakirarie.chatapp.classObjects.Group
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.classObjects.User
import kotlinx.android.synthetic.main.user_row_message.view.*

class DataItem(val data: Any?) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row_message
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if (data is User) {
            viewHolder.itemView.textViewNewMessageUsername.text = data.username
            viewHolder.itemView.statusNewMessage.text = data.status
            Glide.with(viewHolder.itemView.context).load(data.profileImageUrl)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(viewHolder.itemView.imageViewNewMessage)

            viewHolder.itemView.imageViewNewMessage.setOnClickListener {
                val intent = Intent(viewHolder.itemView.context, FullScreenMediaActivity::class.java)
                intent.putExtra("image_url", data.profileImageUrl)
                intent.putExtra("media_type", "image")

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                viewHolder.itemView.context.startActivity(intent)
            }
        } else if (data is Group){
            viewHolder.itemView.textViewNewMessageUsername.text = data.groupName
            viewHolder.itemView.statusNewMessage.text = "Group: ${data.usersList.size} Participants"
            Glide.with(viewHolder.itemView.context).load(data.groupImageUrl)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(viewHolder.itemView.imageViewNewMessage)

            viewHolder.itemView.imageViewNewMessage.setOnClickListener {
                val intent = Intent(viewHolder.itemView.context, FullScreenMediaActivity::class.java)
                intent.putExtra("image_url", data.groupImageUrl)
                intent.putExtra("media_type", "image")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                viewHolder.itemView.context.startActivity(intent)
            }
        }
    }
}