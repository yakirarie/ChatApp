package com.yakirarie.chatapp

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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


class ChatLogActivity : AppCompatActivity() {

    private val TAG = "ChatLogActivityDebug"

    companion object {
        lateinit var toUser: User
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
        supportActionBar?.title = toUser.username

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
        Log.d(TAG, MainActivity.currentUser!!.token)
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser.uid

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
                    } else {
                        adapter.add(ChatToItem(chatMessage, toUser))
                        if (numberOfOldMessages != null) {
                            if (adapter.itemCount > numberOfOldMessages!!)
                                playMessageSound()
                        }

                    }
                    freezeGui(false)
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
            sendBtnChatLog.isClickable = false
            pickImageChatLog.isClickable = false
        } else {
            progressBarChatLog.visibility = View.GONE
            sendBtnChatLog.isClickable = true
            pickImageChatLog.isClickable = true
        }
    }

    fun sendImageClicked(view: View) {
        freezeGui(true)
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPhotoUri = data.data
            uploadImageToFirebaseStorage(selectedPhotoUri!!)
        } else if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            Log.d("IMAGE","image uri ${imageUri.toString()}")
            uploadImageToFirebaseStorage(imageUri!!)
        } else {
            freezeGui(false)

        }
    }

    private fun uploadImageToFirebaseStorage(selectedPhotoUri: Uri) {
        val filename = UUID.randomUUID().toString()
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseStorage.getInstance().getReference("/imagesMessages/$uid/$filename")

        ref.putFile(selectedPhotoUri).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")
                sendImageMessage(it.toString())
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload Image: ${it.message}")
            Toast.makeText(this, "Failed to upload Image: ${it.message}", Toast.LENGTH_SHORT)
                .show()

        }

    }

    private fun sendImageMessage(image: String) {
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = toUser.uid

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
                image,
                fromId,
                toId,
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Calendar.getInstance().time),
                true
            )

        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat from-message: ${ref.key}")
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

    }

    fun openCameraClicked(view: View) {
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
                        if (it != PackageManager.PERMISSION_GRANTED){
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

        val text = editTextChatLog.text.trim().toString()
        if (text.isEmpty()) return

        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = toUser.uid

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
                toId,
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Calendar.getInstance().time),
                false
            )

        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat from-message: ${ref.key}")
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

    }

}
