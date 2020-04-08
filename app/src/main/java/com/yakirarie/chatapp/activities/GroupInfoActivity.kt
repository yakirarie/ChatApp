package com.yakirarie.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yakirarie.chatapp.dialogs.DeleteGroupDialog
import com.yakirarie.chatapp.classObjects.Group
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.adaptersItems.UserItemGroupInfo
import kotlinx.android.synthetic.main.activity_group_info.*


class GroupInfoActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var group: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        supportActionBar?.title = "Group Info"
        group = intent.getParcelableExtra("GROUP_INFO")
        recyclerViewGroupInfo.adapter = adapter
        recyclerViewGroupInfo.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        participantsNumberGroupInfo.text = participantsNumberGroupInfo.text.toString() + group.usersList.size.toString()
        groupNameGroupInfo.text = group.groupName
        Glide.with(this).load(group.groupImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(groupImageGroupInfo)


        fetchUsers()

    }
    private fun fetchUsers() {
        val admin = group.usersList.find { it.uid == group.groupAdminUID }
        group.usersList.remove(admin)
        group.usersList.add(0, admin!!)
        group.usersList.forEach {
            adapter.add(
                UserItemGroupInfo(
                    it,
                    group.groupAdminUID
                )
            )
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_group_edit -> {
                val intent = Intent(this, ChooseUserForGroupActivity::class.java)
                intent.putExtra("GROUP_INFO", group)
                intent.putExtra(MainActivity.CURRENT_USER, MainActivity.currentUser!!)
                startActivity(intent)
            }

            R.id.menu_group_delete -> {
                val bundle = Bundle()
                bundle.putParcelable("GROUP_TO_DELETE", group)
                val customDialog =
                    DeleteGroupDialog()
                customDialog.arguments = bundle
                customDialog.show(supportFragmentManager, "delete group")
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (FirebaseAuth.getInstance().uid!! == group.groupAdminUID)
            menuInflater.inflate(R.menu.nav_group_info, menu)

        return super.onCreateOptionsMenu(menu)
    }


}
