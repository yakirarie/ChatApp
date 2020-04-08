package com.yakirarie.chatapp.listeners

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.yakirarie.chatapp.DataItem
import com.yakirarie.chatapp.Group
import com.yakirarie.chatapp.fragments.NewMessageFragment
import com.yakirarie.chatapp.User

class UsersRecyclerListener {

    val usersMap = HashMap<String, Any>()
    val usersList = mutableListOf<DataItem>()

    fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val data: Any
                if (p0.child("groupName").exists()) {
                    if (p0.getValue(Group::class.java)!!.usersList.none { it.uid == FirebaseAuth.getInstance().uid }) return
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
                    if (p0.getValue(Group::class.java)!!.usersList.none { it.uid == FirebaseAuth.getInstance().uid }) return
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
    private fun refreshRecyclerUsers() {
        NewMessageFragment.adapter.clear()
        usersList.clear()
        usersMap.values.forEach {
            NewMessageFragment.adapter.add(
                DataItem(it)
            )
            usersList.add(DataItem(it))
        }
    }
    fun clearAdapter(){
        NewMessageFragment.adapter.clear()
    }
}