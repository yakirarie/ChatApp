package com.yakirarie.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yakirarie.chatapp.*
import kotlinx.android.synthetic.main.fragment_new_message.*

class NewMessageFragment: Fragment() {
    companion object {
        val USER_KEY = "USER_KEY"
        val GROUP_KEY = "GROUP_KEY"
        val adapter = GroupAdapter<GroupieViewHolder>()

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewNewMessage.adapter =
            adapter
        recyclerViewNewMessage.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setOnItemClickListener { item, view ->
            val dataItem = item as DataItem
            val intent = Intent(view.context, ChatLogActivity::class.java)
            if (dataItem.data is User) {
                intent.putExtra(USER_KEY, dataItem.data)
                startActivity(intent)
            } else if (dataItem.data is Group){
                intent.putExtra(GROUP_KEY, dataItem.data)
                startActivity(intent)
            }
        }
    }
}