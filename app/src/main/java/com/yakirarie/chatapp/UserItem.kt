package com.yakirarie.chatapp

import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.user_row_message.view.*

class UserItem(val user: User?) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row_message
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewNewMessageUsername.text = user!!.username
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.imageViewNewMessage)

        viewHolder.itemView.imageViewNewMessage.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenImage::class.java)
            intent.putExtra("image_url", user.profileImageUrl)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }
}