package com.yakirarie.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    private val TAG = "NewMessageActivityDebug"

    companion object {
        val USER_KEY = "USER_KEY"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    val usersMap = HashMap<String, User>()


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
            val userItem = item as UserItem
            if (userItem.user == null)
                removeDeletedUser(userItem.user!!.uid)
            else {
                val intent = Intent(view.context, ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, userItem.user)
                startActivity(intent)
            }
        }
        fetchUsers()
    }

    private fun refreshRecyclerUsers() {
        adapter.clear()
        usersMap.values.forEach {
            adapter.add(UserItem(it))
        }
    }

    private fun removeDeletedUser(deletedId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("user/$deletedId")
        ref.removeValue().addOnSuccessListener {
            Log.d(TAG, "user $deletedId deleted from users")
            Toast.makeText(this, "This user no longer exists", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete user: ${it.message}")

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

                val user = p0.getValue(User::class.java) ?: return
                if (user.uid != FirebaseAuth.getInstance().uid) {
                    usersMap[p0.key!!] = user
                    refreshRecyclerUsers()
                }

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val user = p0.getValue(User::class.java) ?: return
                if (user.uid != FirebaseAuth.getInstance().uid) {
                    usersMap[p0.key!!] = user
                    refreshRecyclerUsers()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                usersMap.remove(p0.key!!)
                refreshRecyclerUsers()
            }


        }


        )


    }
}
