package com.yakirarie.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yakirarie.chatapp.*
import com.yakirarie.chatapp.activities.ChatLogActivity
import com.yakirarie.chatapp.adaptersItems.DataItem
import com.yakirarie.chatapp.classObjects.Group
import com.yakirarie.chatapp.classObjects.User
import com.yakirarie.chatapp.listeners.UsersRecyclerListener
import kotlinx.android.synthetic.main.fragment_new_message.*
import java.util.*

class NewMessageFragment : Fragment(), SearchView.OnQueryTextListener {
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

        adapter.setOnItemClickListener { item, view ->
            val dataItem = item as DataItem
            val intent = Intent(view.context, ChatLogActivity::class.java)
            if (dataItem.data is User)
                intent.putExtra(USER_KEY, dataItem.data)
            else if (dataItem.data is Group)
                intent.putExtra(GROUP_KEY, dataItem.data)
            searchView.setQuery("", false)
            searchView.clearFocus()
            startActivity(intent)

        }
        searchView.setOnQueryTextListener(this)

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText == null) return false
        val userInput = newText.toLowerCase(Locale.ROOT)
        val newList = mutableListOf<DataItem>()
        for (dataItem in UsersRecyclerListener.dataItemsList)
            if (dataItem.data is User && dataItem.data.username.toLowerCase(Locale.ROOT).startsWith(
                    userInput
                ) || dataItem.data is Group && dataItem.data.groupName.toLowerCase(Locale.ROOT).startsWith(
                    userInput
                )
            )
                newList.add(dataItem)

        adapter.update(newList)
        return true
    }


}