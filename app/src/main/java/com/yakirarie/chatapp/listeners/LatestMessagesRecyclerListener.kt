package com.yakirarie.chatapp.listeners

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.yakirarie.chatapp.classObjects.ChatMessage
import com.yakirarie.chatapp.fragments.HomeFragment
import com.yakirarie.chatapp.adaptersItems.LatestMessageRow

class LatestMessagesRecyclerListener {
    val latestMessagesMap = HashMap<String, ChatMessage>()
    lateinit var listener: ChildEventListener

    fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        listener = (object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage

                refreshRecyclerViewMessages()

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                latestMessagesMap.remove(p0.key!!)
                refreshRecyclerViewMessages()

            }

        })
        ref.addChildEventListener(listener)
    }

    private fun refreshRecyclerViewMessages() {
        HomeFragment.adapter.clear()
        latestMessagesMap.values.forEach {
            if (it.toId.size > 1) {
                HomeFragment.adapter.add(
                    LatestMessageRow(
                        it,
                        latestMessagesMap.filterValues { chatMessage -> it.id == chatMessage.id }.keys.elementAt(
                            0
                        )
                    )
                )
            } else
                HomeFragment.adapter.add(
                    LatestMessageRow(it)
                )

        }
    }

    fun clearAdapter(){
        FirebaseDatabase.getInstance()
            .getReference("/latest-messages/${FirebaseAuth.getInstance().uid}")
            .removeEventListener(listener)
        HomeFragment.adapter.clear()
    }
}