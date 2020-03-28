package com.yakirarie.chatapp

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.text.SimpleDateFormat
import java.util.*


class ChatLogActivity : AppCompatActivity() {

    private val TAG = "ChatLogActivityDebug"

    companion object {
        lateinit var toUser: User
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var player: MediaPlayer? = null
    var numberOfOldMessages: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        initPlayer()
        recyclerViewChatLog.adapter = adapter
        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser.username

        listenForMessages()
    }

    private fun playMessageSound() {
        if (player != null)
            player!!.start()

    }

    private fun initPlayer() {
        if (player == null)
            player = MediaPlayer.create(applicationContext, R.raw.notification_sound)
    }
    
    private fun stopMessageSound() {
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    override fun onStop() {
        super.onStop()
        stopMessageSound()
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }


    private fun listenForMessages() {
        Log.d(TAG, MainActivity.currentUser!!.token)
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                numberOfOldMessages = p0.childrenCount.toInt()
            }

        })
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
                        adapter.add(ChatFromItem(chatMessage.text, chatMessage.timestamp, MainActivity.currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, chatMessage.timestamp, toUser))
                        if (numberOfOldMessages != null) {
                            if (adapter.itemCount > numberOfOldMessages!!)
                                playMessageSound()
                        }

                    }
                }

                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


    fun sendBtnClicked(view: View) {

        val text = editTextChatLog.text.toString()
        if (text.isEmpty()) return

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

        val chatMessage =
            ChatMessage(ref.key!!, text, fromId, toId, SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().time))

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
