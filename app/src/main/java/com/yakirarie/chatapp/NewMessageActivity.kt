package com.yakirarie.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import androidx.appcompat.widget.SearchView
import kotlinx.android.synthetic.main.activity_new_message.*
import java.util.*
import kotlin.collections.HashMap

class NewMessageActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val TAG = "NewMessageActivityDebug"

    companion object {
        val USER_KEY = "USER_KEY"
        val GROUP_KEY = "GROUP_KEY"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    val usersMap = HashMap<String, Any>()
    val usersList = mutableListOf<DataItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"
        recyclerViewNewMessage.adapter = adapter
        recyclerViewNewMessage.addItemDecoration(
            DividerItemDecoration(
                this,
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

        fetchUsers()
    }

    private fun refreshRecyclerUsers() {
        adapter.clear()
        usersList.clear()
        usersMap.values.forEach {
            adapter.add(DataItem(it))
            usersList.add(DataItem(it))
        }
    }


    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val data: Any
                if (p0.child("groupName").exists()) {
                    data = p0.getValue(Group::class.java) ?: return
                    usersMap[p0.key!!] = data
                } else {
                    data = p0.getValue(User::class.java) ?: return
                    if (data.uid != FirebaseAuth.getInstance().uid)
                        usersMap[p0.key!!] = data
                }

                refreshRecyclerUsers()

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data: Any
                if (p0.child("groupName").exists()) {
                    data = p0.getValue(Group::class.java) ?: return
                    usersMap[p0.key!!] = data
                } else {
                    data = p0.getValue(User::class.java) ?: return
                    if (data.uid != FirebaseAuth.getInstance().uid)
                        usersMap[p0.key!!] = data
                }

                refreshRecyclerUsers()

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                usersMap.remove(p0.key!!)
                refreshRecyclerUsers()
            }


        }


        )


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_search, menu)
        val menuItem = menu!!.findItem(R.id.menu_search)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        if (p0 == null) return false
        val userInput = p0.toLowerCase(Locale.ROOT)
        val newList = mutableListOf<DataItem>()
        for (userItem in usersList) {
            if (userItem.data is User) {
                if (userItem.data.username.toLowerCase(Locale.ROOT).startsWith(userInput)) {
                    newList.add(userItem)
                }
            } else if (userItem.data is Group) {
                if (userItem.data.groupName.toLowerCase(Locale.ROOT).startsWith(userInput)) {
                    newList.add(userItem)
                }
            }
        }
        adapter.update(newList)
        return true
    }
}
