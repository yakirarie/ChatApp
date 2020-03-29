package com.yakirarie.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import androidx.appcompat.widget.SearchView
import kotlinx.android.synthetic.main.activity_new_message.*
import java.util.*
import kotlin.collections.HashMap

class NewMessageActivity : AppCompatActivity(), SearchView.OnQueryTextListener{

    private val TAG = "NewMessageActivityDebug"

    companion object {
        val USER_KEY = "USER_KEY"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    val usersMap = HashMap<String, User>()
    val usersList = mutableListOf<UserItem>()


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
        usersList.clear()
        usersMap.values.forEach {
            adapter.add(UserItem(it))
            usersList.add(UserItem(it))
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_search, menu)
        val menuItem = menu!!.findItem(R.id.menu_search)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        if (p0 == null) return false
        val userInput = p0.toLowerCase(Locale.ROOT)
        val newList = mutableListOf<UserItem>()
        for (userItem in usersList){
            if (userItem.user!!.username.toLowerCase(Locale.ROOT).contains(userInput)){
                newList.add(userItem)
            }
        }
        adapter.update(newList)
        return true
    }
}
