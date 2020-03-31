package com.yakirarie.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityDebug"

    companion object {
        var currentUser: User? = null
        val CURRENT_USER = "CURRENT_USER"

    }


    val adapter = GroupAdapter<GroupieViewHolder>()
    val latestMessagesMap = HashMap<String, ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = ""
        recyclerViewLatestMessages.adapter = adapter
        recyclerViewLatestMessages.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            if (row.chatPartnerUser == null)
                if (row.chatMessage.fromId == FirebaseAuth.getInstance().uid)
                    removeDeletedUserInteractionsWithYou(row.chatMessage.toId[0])
                else
                    removeDeletedUserInteractionsWithYou(row.chatMessage.fromId)
            else {
                intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
                startActivity(intent)
            }
        }
        listenForLatestMessages()
        verifyUserLoggedIn()
        fetchCurrentUser()
        receivedNotification()


    }

    override fun onStart() {
        super.onStart()
        verifyUserLoggedIn()
        fetchCurrentUser()

    }

    private fun removeDeletedUserInteractionsWithYou(deletedId: String) {
        Toast.makeText(this, "This user no longer exists", Toast.LENGTH_SHORT).show()
        val uid = FirebaseAuth.getInstance().uid
        val refLatest = FirebaseDatabase.getInstance().getReference("latest-messages/$uid/$deletedId")
        refLatest.removeValue().addOnSuccessListener {
            Log.d(TAG, "user $deletedId deleted from your latest messages")
            Toast.makeText(this, "This user no longer exists", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete latest-messages with deleted : ${it.message}")

        }

        val refMessages = FirebaseDatabase.getInstance().getReference("user-messages/$uid/$deletedId")
        refMessages.removeValue().addOnSuccessListener {
            Log.d(TAG, "user $deletedId deleted from your messages")

        }.addOnFailureListener {
            Log.e(TAG, "Failed to delete user-messages with deleted : ${it.message}")

        }


    }

    private fun checkIfTokenHasChanged() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Log.d(TAG, " token: ${it.token}")
            if (currentUser!!.token != it.token) {
                val ref = FirebaseDatabase.getInstance().getReference("users/${currentUser!!.uid}")
                ref.child("token").setValue(it.token).addOnSuccessListener {
                    Log.d(TAG, "Token updated successfully!")
                }.addOnFailureListener {
                    Log.e(TAG, "Unable to update token: ${it.message}")
                }
            }
        }.addOnFailureListener {
            Log.e(TAG, "Unable to get token: ${it.message}")
            Toast.makeText(
                this,
                "Unable to get token: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()

        }

    }

    private fun receivedNotification() {
        val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        if (toUser != null) {  // my notification
            if (currentUser == null) {
                currentUser = intent.getParcelableExtra(CURRENT_USER)

            }
            Log.d("USER", toUser.toString())

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY, toUser)
            startActivity(intent)
        } else { // firebase notification
            val senderId = intent.getStringExtra("sender_id")
            val senderUsername = intent.getStringExtra("sender_username")
            val senderImg = intent.getStringExtra("sender_img")
            val senderToken = intent.getStringExtra("sender_token")

            val receiverId = intent.getStringExtra("receiver_id")
            val receiverUsername = intent.getStringExtra("receiver_username")
            val receiverImg = intent.getStringExtra("receiver_img")
            val receiverToken = intent.getStringExtra("receiver_token")

            if (receiverId != null && receiverUsername != null && receiverImg != null && receiverToken != null)
                currentUser = User(receiverId, receiverUsername, receiverImg, receiverToken)

            if (senderId != null && senderUsername != null && senderImg != null && senderToken != null) {
                val user = User(senderId, senderUsername, senderImg, senderToken)

                val intent = Intent(this, ChatLogActivity::class.java)
                intent.putExtra(NewMessageActivity.USER_KEY, user)


                startActivity(intent)
            }


        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
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
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            Log.d("test", it.toString())
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                supportActionBar?.title = currentUser?.username
                Log.d(TAG, "current user is ${currentUser?.username}")
                checkIfTokenHasChanged()

            }

        })

    }

    private fun verifyUserLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                SignOutDialog().show(supportFragmentManager, "sign_out")
            }
            R.id.menu_user_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_create_group -> {
                if (currentUser == null){
                    Toast.makeText(this, "Couldn't load your details, please try again", Toast.LENGTH_SHORT).show()
                    return false
                }
                val intent = Intent(this, ChooseUserForGroupActivity::class.java)
                intent.putExtra(MainActivity.CURRENT_USER, currentUser)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }
}
