package com.yakirarie.chatapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*


class ChatLogActivity : AppCompatActivity() {

    private val TAG = "ChatLogActivityDebug"
    lateinit var toUser: User
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerViewChatLog.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser.username
        listenForMessages()

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage =
                    p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    if (chatMessage.fromId == fromId) {
                        adapter.add(ChatFromItem(chatMessage.text, MainActivity.currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser))
                    }
                }

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


    fun sendBtnClicked(view: View) {
        val text = editTextChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = toUser.uid

        val ref =
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val latestMessagesRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        val latestMessagesToRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat from-message: ${ref.key}")
            editTextChatLog.text.clear()
            recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
        }

        toRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat to-message: ${toRef.key}")
        }

        latestMessagesRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat latest-from-message: ${latestMessagesRef.key}")
        }

        latestMessagesToRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat latest-to-message: ${latestMessagesToRef.key}")
        }

    }

}
