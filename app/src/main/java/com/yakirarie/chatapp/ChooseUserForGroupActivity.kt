package com.yakirarie.chatapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_choose_users_for_group.*
import java.util.*

class ChooseUserForGroupActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    val usersMap = HashMap<String, User>()
    private val usersList = mutableListOf<UserItemCreateGroup>()
    private var chosenUsersIds = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_users_for_group)
        val currentUser = intent.getParcelableExtra<User>(MainActivity.CURRENT_USER)
        val currentGroup = intent.getParcelableExtra<Group>("GROUP_INFO")

        supportActionBar?.title = if (currentGroup == null) "Create Group" else "Edit Group"

        currentGroup?.usersList?.forEach {
            if (it.uid != FirebaseAuth.getInstance().uid)
                chosenUsersIds.add(it.uid)
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelableArrayList<User>("CHOSEN_USERS") != null) {
                chosenUsersIds.clear()
                chosenUsersIds.addAll(savedInstanceState.getStringArrayList("CHOSEN_USERS") as Collection<String>)

            }
        }

        recyclerViewChooseUsers.adapter = adapter
        recyclerViewChooseUsers.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setOnItemClickListener { item, view ->
            val userItemCreateGroup = item as UserItemCreateGroup

            if (!chosenUsersIds.contains(userItemCreateGroup.user!!.uid)) {
                chosenUsersIds.add(userItemCreateGroup.user.uid)
                view.background = resources.getDrawable(R.drawable.gradient_chosen, null)
            } else {
                chosenUsersIds.remove(userItemCreateGroup.user.uid)
                view.setBackgroundColor(Color.WHITE)
            }


        }
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_LTR) {
            floatingActionButtonNext.rotation = 180f
        }

        floatingActionButtonNext.setOnClickListener {

            if (chosenUsersIds.size < 2)
                Toast.makeText(this, "Please choose at list two users", Toast.LENGTH_LONG).show()
            else {
                val chosenUsersList = arrayListOf<User>()
                usersList.forEach {
                    if (chosenUsersIds.contains(it.user!!.uid))
                        chosenUsersList.add(it.user)
                }
                chosenUsersList.add(currentUser)
                val intent = Intent(this, CreateGroupActivity::class.java)
                intent.putParcelableArrayListExtra("CHOSEN_USERS", chosenUsersList)
                if (currentGroup != null)
                    intent.putExtra("GROUP_INFO", currentGroup)
                startActivity(intent)

            }
        }
        fetchUsers()
    }

    private fun refreshRecyclerUsers() {
        adapter.clear()
        usersList.clear()
        usersMap.values.forEach {
            adapter.add(UserItemCreateGroup(it, chosenUsersIds))
            usersList.add(UserItemCreateGroup(it, chosenUsersIds))
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
                if (p0.child("groupName").exists()) return
                val user = p0.getValue(User::class.java) ?: return
                if (user.uid != FirebaseAuth.getInstance().uid) {
                    usersMap[p0.key!!] = user
                    refreshRecyclerUsers()
                }

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if (p0.child("groupName").exists()) return
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
        val newList = mutableListOf<UserItemCreateGroup>()
        for (UserItemCreateGroup in usersList) {
            if (UserItemCreateGroup.user!!.username.toLowerCase(Locale.ROOT).startsWith(userInput)) {
                newList.add(UserItemCreateGroup)
            }
        }
        adapter.update(newList)
        return true
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("CHOSEN_USERS", chosenUsersIds)

    }

}
