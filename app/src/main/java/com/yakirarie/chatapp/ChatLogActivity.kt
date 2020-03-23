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

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser.uid
        val ref = FirebaseFirestore.getInstance().collection("user-messages/$fromId/$toId")
        ref.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.d(TAG, "Listening failed: $firebaseFirestoreException");
                return@addSnapshotListener
            }
            if (querySnapshot != null) {
                for (documentChange in querySnapshot.documentChanges){
                    when (documentChange.type){
                        DocumentChange.Type.ADDED -> {
                            val chatMessage = documentChange.document.toObject(ChatMessage::class.java)
                            if (chatMessage.fromId == fromId){
                                adapter.add(ChatFromItem(chatMessage.text, MainActivity.currentUser!!))
                            }
                            else{
                                adapter.add(ChatToItem(chatMessage.text, toUser))
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
        val toId = toUser.uid
        val ref = FirebaseFirestore.getInstance().collection("user-messages/$fromId/$toId")
        val toRef = FirebaseFirestore.getInstance().collection("user-messages/$toId/$fromId")


        val chatMessage = ChatMessage(ref.id, text, fromId, toId, System.currentTimeMillis() / 1000)
        ref.add(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat from-message: ${ref.id}")
            editTextChatLog.text.clear()
            recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
        }

        toRef.add(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat to-message: ${toRef.id}")
        }

    }

}
