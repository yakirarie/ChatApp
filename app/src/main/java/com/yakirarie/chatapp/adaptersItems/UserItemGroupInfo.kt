package com.yakirarie.chatapp.adaptersItems

import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.yakirarie.chatapp.activities.FullScreenMediaActivity
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.classObjects.User
import kotlinx.android.synthetic.main.user_create_group.view.*

class UserItemGroupInfo(val user: User, val groupAdminUID: String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_create_group
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if (user.uid == groupAdminUID)
                viewHolder.itemView.adminIcon.visibility = View.VISIBLE
        viewHolder.itemView.textViewCreateGroupUsername.text = user.username
        Glide.with(viewHolder.itemView.context).load(user.profileImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(viewHolder.itemView.imageViewCreateGroup)

        viewHolder.itemView.imageViewCreateGroup.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FullScreenMediaActivity::class.java)
            intent.putExtra("image_url", user.profileImageUrl)
            intent.putExtra("media_type", "image")

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            viewHolder.itemView.context.startActivity(intent)
        }
    }
}