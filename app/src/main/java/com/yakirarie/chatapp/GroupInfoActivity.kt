package com.yakirarie.chatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_group_info.*


class GroupInfoActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var group: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        supportActionBar?.title = "Group Info"
        group = intent.getParcelableExtra("GROUP_INFO")
        recyclerViewGroupInfo.adapter = adapter
        recyclerViewGroupInfo.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        participantsNumberGroupInfo.text = participantsNumberGroupInfo.text.toString() + group.usersList.size.toString()
        groupNameGroupInfo.text = group.groupName
        Glide.with(this).load(group.groupImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(groupImageGroupInfo)


        fetchUsers()

    }
    private fun fetchUsers() {
        val admin = group.usersList.find { it.uid == group.groupAdminUID }
        group.usersList.remove(admin)
        group.usersList.add(0, admin!!)
        group.usersList.forEach {
            adapter.add(UserItemGroupInfo(it, group.groupAdminUID))
        }
    }


}
