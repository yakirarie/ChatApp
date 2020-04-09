package com.yakirarie.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.yakirarie.chatapp.classObjects.Group
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.dialogs.SignOutDialog
import com.yakirarie.chatapp.classObjects.User
import com.yakirarie.chatapp.fragments.HomeFragment
import com.yakirarie.chatapp.fragments.MyProfileFragment
import com.yakirarie.chatapp.fragments.NewMessageFragment
import com.yakirarie.chatapp.listeners.LatestMessagesRecyclerListener
import com.yakirarie.chatapp.listeners.UsersRecyclerListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityDebug"

    companion object {
        var currentUser: User? = null
        val CURRENT_USER = "CURRENT_USER"
    }
    private val usersRecyclerListener =
        UsersRecyclerListener()
    private val latestMessagesRecyclerListener =
        LatestMessagesRecyclerListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = ""

        verifyUserLoggedIn()
        fetchCurrentUser()
        receivedNotification()

        supportFragmentManager.beginTransaction()
            .replace(frameLayoutMainActivity.id,
                HomeFragment()
            ).commit()

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(frameLayoutMainActivity.id,
                            HomeFragment()
                        ).commit()
                    true
                }
                R.id.menu_user_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(frameLayoutMainActivity.id,
                            MyProfileFragment()
                        ).commit()
                    true
                }
                R.id.menu_new_message -> {
                    supportFragmentManager.beginTransaction()
                        .replace(frameLayoutMainActivity.id,
                            NewMessageFragment()
                        ).commit()
                    true
                }
                else -> false
            }
        }

    }

    override fun onStart() {
        super.onStart()
        verifyUserLoggedIn()
        fetchCurrentUser()
        usersRecyclerListener.fetchUsers()
        latestMessagesRecyclerListener.listenForLatestMessages()

    }

    override fun onStop() {
        super.onStop()
        latestMessagesRecyclerListener.clearAdapter()
        usersRecyclerListener.clearAdapter()

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
        val toUser = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)
        val toGroup = intent.getParcelableExtra<Group>(NewMessageFragment.GROUP_KEY)

        if (toUser != null) {  // my notification
            if (currentUser == null)
                currentUser = intent.getParcelableExtra(
                    CURRENT_USER
                )

            val intent = Intent(this, ChatLogActivity::class.java)

            if (toGroup != null) // group
                intent.putExtra(NewMessageFragment.GROUP_KEY, toGroup)
            else  //user
                intent.putExtra(NewMessageFragment.USER_KEY, toUser)
            startActivity(intent)

        } else { // firebase notification
            val senderId = intent.getStringExtra("sender_id")
            val senderUsername = intent.getStringExtra("sender_username")
            val senderImg = intent.getStringExtra("sender_img")
            val senderToken = intent.getStringExtra("sender_token")

            if (intent.getStringExtra("group_id") != null) { // group msg
                val groupId = intent.getStringExtra("group_id")
                val groupName = intent.getStringExtra("group_name")
                val groupImage = intent.getStringExtra("group_image")
                val groupSize = intent.getStringExtra("group_size").toInt()
                val usersList = arrayListOf<User>()
                for (i in 0 until groupSize) {
                    val user = User(
                        intent.getStringExtra("receiver_id${i + 1}"),
                        intent.getStringExtra("receiver_username${i + 1}"),
                        intent.getStringExtra("receiver_img${i + 1}"),
                        intent.getStringExtra("receiver_token${i + 1}")
                    )
                    usersList.add(user)
                }
                currentUser = usersList.find { it.uid == FirebaseAuth.getInstance().uid }
                val group = Group(
                    groupId,
                    intent.getStringExtra("group_admin_uid"),
                    groupName,
                    groupImage,
                    usersList
                )
                val intent = Intent(this, ChatLogActivity::class.java)
                intent.putExtra(NewMessageFragment.GROUP_KEY, group)
                startActivity(intent)

            } else { // user to user msg
                val receiverId = intent.getStringExtra("receiver_id")
                val receiverUsername = intent.getStringExtra("receiver_username")
                val receiverImg = intent.getStringExtra("receiver_img")
                val receiverToken = intent.getStringExtra("receiver_token")

                if (receiverId != null && receiverUsername != null && receiverImg != null && receiverToken != null)
                    currentUser =
                        User(
                            receiverId,
                            receiverUsername,
                            receiverImg,
                            receiverToken
                        )

                if (senderId != null && senderUsername != null && senderImg != null && senderToken != null) {
                    val user = User(
                        senderId,
                        senderUsername,
                        senderImg,
                        senderToken
                    )

                    val intent = Intent(this, ChatLogActivity::class.java)
                    intent.putExtra(NewMessageFragment.USER_KEY, user)
                    startActivity(intent)
                }
            }


        }
    }


    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(
                    User::class.java)
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

            R.id.menu_sign_out -> {
                SignOutDialog()
                    .show(supportFragmentManager, "sign_out")
            }

            R.id.menu_create_group -> {
                if (currentUser == null) {
                    Toast.makeText(
                        this,
                        "Couldn't load your details, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
                val intent = Intent(this, ChooseUserForGroupActivity::class.java)
                intent.putExtra(
                    CURRENT_USER,
                    currentUser
                )
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
