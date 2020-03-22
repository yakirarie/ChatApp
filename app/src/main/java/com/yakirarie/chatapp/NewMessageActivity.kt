package com.yakirarie.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    private val TAG = "NewMessageActivityDebug"

    companion object{
        val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers(){
        val ref = FirebaseFirestore.getInstance().collection("users")
        ref.get()
            .addOnSuccessListener { result ->
                val adapter = GroupAdapter<GroupieViewHolder>()

                for (document in result) {
                    val user = document.toObject(User::class.java)
                    adapter.add(UserItem(user))
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()

                }

                recyclerViewNewMessage.adapter = adapter

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents.", exception)
            }
    }
}
