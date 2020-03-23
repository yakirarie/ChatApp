package com.yakirarie.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private val TAG = "ChatLogActivityDebug"
    lateinit var user: User
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerViewChatLog.adapter = adapter
        user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user.username
        listenForMessages()

    }

    private fun listenForMessages(){
        val ref = FirebaseFirestore.getInstance().collection("messages")
        ref.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.d(TAG, "Listen failed: $firebaseFirestoreException");
                return@addSnapshotListener
            }
            if (querySnapshot != null) {
                for (documentChange in querySnapshot.documentChanges){
                    when (documentChange.type){
                        DocumentChange.Type.ADDED -> {
                            val chatMessage = documentChange.document.toObject(ChatMessage::class.java)
                            if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                                adapter.add(ChatFromItem(chatMessage.text, MainActivity.currentUser!!))
                            }
                            else{
                                adapter.add(ChatToItem(chatMessage.text, user))
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {}
                        DocumentChange.Type.REMOVED -> {}

                    }
                }


            }
        }

}

    fun sendBtnClicked(view: View) {
        val text = editTextChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = user.uid
        val ref = FirebaseFirestore.getInstance().collection("messages").document()
        val chatMessage = ChatMessage(ref.id, text, fromId, toId, System.currentTimeMillis() / 1000)
        ref.set(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${ref.id}")
        }
    }

}
