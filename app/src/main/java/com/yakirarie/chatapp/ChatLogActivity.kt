package com.yakirarie.chatapp

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatLogActivity : AppCompatActivity() {

    private val TAG = "ChatLogActivityDebug"

    companion object {
        var toUser: User? = null
        var toGroup: Group? = null
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var player: MediaPlayer? = null
    var numberOfOldMessages: Int? = null

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        initPlayer()
        recyclerViewChatLog.adapter = adapter
        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        toGroup = intent.getParcelableExtra(NewMessageActivity.GROUP_KEY)
        if (toUser != null)
            supportActionBar?.title = toUser!!.username
        else if (toGroup != null)
            supportActionBar?.title = toGroup!!.groupName
        listenForMessages()
    }

    private fun playMessageSound() {
        if (player != null)
            player!!.start()

    }

    private fun initPlayer() {
        if (player == null)
            player = MediaPlayer.create(applicationContext, R.raw.notification_sound)
    }

    private fun stopMessageSound() {
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    override fun onStop() {
        super.onStop()
        stopMessageSound()
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }


    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = toUser?.uid
        val toIds = toGroup?.usersList?.filter {
            it.uid != fromId
        }

        if (toId != null) {
            handleTwoUsersChat(fromId, toId)
        } else if (toIds != null) {
            handleGroupChat(fromId, toIds as ArrayList<User>)
        }


    }

    private fun handleTwoUsersChat(fromId: String, toId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                numberOfOldMessages = p0.childrenCount.toInt()
            }

        })
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage =
                    p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (chatMessage.fromId == fromId) {
                        adapter.add(ChatFromItem(chatMessage, MainActivity.currentUser!!))
                        if (chatMessage.messageType != "text")
                            freezeGui(false)

                    } else {
                        adapter.add(ChatToItem(chatMessage, toUser!!))

                        if (numberOfOldMessages != null) {
                            if (adapter.itemCount > numberOfOldMessages!!)
                                playMessageSound()
                        }

                    }
                }

                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    private fun handleGroupChat(fromId: String, toIds: ArrayList<User>) {
        val ref = FirebaseDatabase.getInstance().getReference("/group-messages/${toGroup!!.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                numberOfOldMessages = p0.childrenCount.toInt()
            }

        })
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage =
                    p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (chatMessage.fromId == fromId) {
                        adapter.add(ChatFromItem(chatMessage, MainActivity.currentUser!!))
                        if (chatMessage.messageType != "text")
                            freezeGui(false)

                    } else {
                        adapter.add(
                            ChatToItem(
                                chatMessage,
                                toIds.find { it.uid == chatMessage.fromId }!!
                            )
                        )

                        if (numberOfOldMessages != null) {
                            if (adapter.itemCount > numberOfOldMessages!!)
                                playMessageSound()
                        }

                    }
                }

                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    private fun freezeGui(toFreeze: Boolean) {
        if (toFreeze) {
            progressBarChatLog.visibility = View.VISIBLE
            pickImageChatLog.isClickable = false
            openCameraChatLog.isClickable = false
        } else {
            progressBarChatLog.visibility = View.GONE
            pickImageChatLog.isClickable = true
            openCameraChatLog.isClickable = true
        }
    }

    fun sendImageClicked(view: View) {
        if (toGroup != null) return
        freezeGui(true)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/* video/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPhotoUri = data.data
            uploadImageToFirebaseStorage(selectedPhotoUri!!)
        } else if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            uploadImageToFirebaseStorage(imageUri!!)
        } else {
            freezeGui(false)

        }
    }

    private fun uploadImageToFirebaseStorage(selectedPhotoUri: Uri) {
        val messageType = when {
            selectedPhotoUri.toString().contains("image") -> {
                "image"
            }
            selectedPhotoUri.toString().contains("video") -> {
                "video"
            }
            else -> "image"
        }
        val filename = UUID.randomUUID().toString()
        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseStorage.getInstance().getReference("/Media Messages/$uid/$filename")

        ref.putFile(selectedPhotoUri).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")
                sendMessage(it.toString(), messageType)
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload Image: ${it.message}")
            Toast.makeText(this, "Failed to upload Image: ${it.message}", Toast.LENGTH_SHORT)
                .show()

        }

    }

    private fun sendMessage(media: String? = null, messageType: String) {
        val text: String
        if (media == null) {
            text = editTextChatLog.text.trim().toString()
            if (text.isEmpty()) return
        } else
            text = media

        val fromId = FirebaseAuth.getInstance().uid ?: return
        if (toUser != null) {
            val toId = toUser!!.uid

            val ref =
                FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
            val toRef =
                FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
            val latestMessagesRef =
                FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
            val latestMessagesToRef =
                FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

            val chatMessage =
                ChatMessage(
                    ref.key!!,
                    text,
                    fromId,
                    mutableListOf(toId),
                    SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Calendar.getInstance().time),
                    messageType
                )

            ref.setValue(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved our chat from-message: ${ref.key}")
                if (chatMessage.messageType == "text")
                    editTextChatLog.text.clear()
                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
            }

            toRef.setValue(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved our chat to-message: ${toRef.key}")
            }

            latestMessagesRef.setValue(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved our chat latest-from-message: ${latestMessagesRef.key}")
            }

            latestMessagesToRef.setValue(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved our chat latest-to-message: ${latestMessagesToRef.key}")
            }
        } else if (toGroup != null) {
            val toIds = ArrayList<String>()
            toGroup!!.usersList.forEach {
                if (it.uid != FirebaseAuth.getInstance().uid)
                    toIds.add(it.uid)
            }
            val ref =
                FirebaseDatabase.getInstance().getReference("/group-messages/${toGroup!!.uid}")
                    .push()


            val chatMessage =
                ChatMessage(
                    ref.key!!,
                    text,
                    fromId,
                    toIds,
                    SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Calendar.getInstance().time),
                    messageType
                )

            ref.setValue(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved our chat from-message: ${ref.key}")
                if (chatMessage.messageType == "text")
                    editTextChatLog.text.clear()
                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
            }

            for (user in toGroup!!.usersList) {
                if (user.uid != fromId) {
                    val latestMessagesRef =
                        FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/${user.uid}/${toGroup!!.uid}")
                    latestMessagesRef.setValue(chatMessage).addOnSuccessListener {
                        Log.d(TAG, "Saved our chat latest-from-message: ${latestMessagesRef.key}")
                    }
                } else {
                    val latestMessagesRef =
                        FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/$fromId/${toGroup!!.uid}")
                    latestMessagesRef.setValue(chatMessage).addOnSuccessListener {
                        Log.d(TAG, "Saved our chat latest-from-message: ${latestMessagesRef.key}")
                    }
                }
            }
        }


    }


    fun openCameraClicked(view: View) {
        if (toGroup != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, PERMISSION_CODE)

            } else {
                openCamera()
            }
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        freezeGui(true)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From The Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty()) {
                    grantResults.forEach {
                        if (it != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    openCamera()
                }
            }
        }
    }


    fun sendBtnClicked(view: View) {
        sendMessage(messageType = "text")

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_group_info -> {
                val intent = Intent(this, GroupInfoActivity::class.java)
                intent.putExtra("GROUP_INFO", toGroup)
                startActivity(intent)
            }

            R.id.menu_group_edit -> {
                if (FirebaseAuth.getInstance().uid!! != toGroup!!.groupAdminUID){
                    Toast.makeText(this, "Only an admin can edit a group", Toast.LENGTH_SHORT).show()
                    return false
                }
                val intent = Intent(this, ChooseUserForGroupActivity::class.java)
                intent.putExtra("GROUP_INFO", toGroup)
                intent.putExtra(MainActivity.CURRENT_USER, MainActivity.currentUser!!)
                startActivity(intent)
            }

            R.id.menu_group_delete -> {
                if (FirebaseAuth.getInstance().uid!! != toGroup!!.groupAdminUID){
                    Toast.makeText(this, "Only an admin can delete a group", Toast.LENGTH_SHORT).show()
                    return false
                }
                val bundle = Bundle()
                bundle.putParcelable("GROUP_TO_DELETE", toGroup)
                val customDialog = DeleteGroupDialog()
                customDialog.arguments = bundle
                customDialog.show(supportFragmentManager, "delete group")
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (toGroup != null)
            menuInflater.inflate(R.menu.nav_group, menu)

        return super.onCreateOptionsMenu(menu)
    }


}
