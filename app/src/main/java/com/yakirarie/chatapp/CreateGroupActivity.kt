package com.yakirarie.chatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_create_group.*
import java.util.*
import kotlin.collections.ArrayList

class CreateGroupActivity : AppCompatActivity() {

    lateinit var chosenUsers: ArrayList<User>
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val TAG = "CreateGroupDebug"
    private var selectedPhotoUri: Uri? = null
    private var currentGroup: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        chosenUsers = intent.getParcelableArrayListExtra<User>("CHOSEN_USERS")
        currentGroup = intent.getParcelableExtra("GROUP_INFO")
        supportActionBar?.title = if (currentGroup == null) "Create Group" else "Edit Group"


        if (currentGroup != null){
            Glide.with(this).load(currentGroup!!.groupImageUrl)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(changeProfileCreateGroup)
            groupNameCreateGroup.setText(currentGroup!!.groupName)
        }

        recyclerViewCreateGroup.adapter = adapter
        recyclerViewCreateGroup.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        val admin = chosenUsers.find { it.uid == FirebaseAuth.getInstance().uid!! }
        chosenUsers.remove(admin)
        chosenUsers.add(0, admin!!)
        for (user in chosenUsers)
            adapter.add(UserItemGroupInfo(user, FirebaseAuth.getInstance().uid!!))


    }


    fun uploadPhotoBtn(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            Glide.with(this).load(bitmap)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(changeProfileCreateGroup)
            defaultProfileCreateGroup.alpha = 0f

        }
    }

    fun createGroupBtn(view: View) {
        if (groupNameCreateGroup.text.isEmpty()) {
            Toast.makeText(this, "Please provide a group name", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentGroup == null) {
            if (selectedPhotoUri == null) {
                Toast.makeText(this, "Please provide a group image", Toast.LENGTH_SHORT).show()
                return
            }
            freezeGui(true)
            val groupId = UUID.randomUUID().toString()
            uploadImageToFirebaseStorage(groupId)
        }
        else{
            freezeGui(true)
            if (selectedPhotoUri != null)
                uploadImageToFirebaseStorage(currentGroup!!.uid)
            else
                saveGroupToFirebaseDataBase(currentGroup!!.uid, currentGroup!!.groupImageUrl)
        }


    }

    private fun uploadImageToFirebaseStorage(groupId: String) {
        val ref = FirebaseStorage.getInstance().getReference("/Groups Profiles/$groupId")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")
                saveGroupToFirebaseDataBase(groupId, it.toString())

            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload Image: ${it.message}")
            Toast.makeText(this, "Failed to upload Image: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            freezeGui(false)

        }

    }

    private fun freezeGui(toFreeze: Boolean) {
        if (toFreeze) {
            progressBarCreateGroup.visibility = View.VISIBLE
            selectPhotoCreateGroup.isClickable = false
            confirmGroupCreateGroup.isClickable = false
        } else {
            progressBarCreateGroup.visibility = View.GONE
            selectPhotoCreateGroup.isClickable = true
            confirmGroupCreateGroup.isClickable = true
        }
    }

    private fun saveGroupToFirebaseDataBase(groupId: String, groupImageUrl: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$groupId")
        val group = Group(groupId, FirebaseAuth.getInstance().uid!!, groupNameCreateGroup.text.toString(), groupImageUrl, chosenUsers)
        ref.setValue(group).addOnSuccessListener {
            Log.d(TAG, "Successfully saved group to Firebase!")
            val msg = if (currentGroup == null) "created" else "edited"

            Toast.makeText(this, "Your group has been $msg successfully!", Toast.LENGTH_SHORT)
                .show()
            freezeGui(false)

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }.addOnFailureListener {
            Log.d(TAG, "Failed to save group to Firebase: ${it.message}")
            val msg = if (currentGroup == null) "create" else "edit"

            Toast.makeText(
                this,
                "Failed to $msg your group: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()
            freezeGui(false)


        }
    }
}
