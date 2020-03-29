package com.yakirarie.chatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_my_profile.changeProfileMyProfile
import kotlinx.android.synthetic.main.activity_my_profile.usernameMyProfile
import java.util.*

class MyProfileActivity : AppCompatActivity() {

    private val TAG = "MyProfileActivityDebug"
    private var currentUser: User? = null
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        supportActionBar?.title = "My Profile"
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)!!
                usernameMyProfile.setText(currentUser!!.username)
                Glide.with(applicationContext).load(currentUser!!.profileImageUrl)
                    .placeholder(R.drawable.ic_loading_sign)
                    .error(R.drawable.ic_error_sign).diskCacheStrategy(
                        DiskCacheStrategy.ALL
                    ).into(changeProfileMyProfile)

            }

        })

    }

    fun deleteAccountClicked(view: View) {
        if (currentUser == null) return
        val bundle = Bundle()
        bundle.putParcelable("USER_TO_DELETE", currentUser)
        val customDialog = DeleteUserDialog()
        customDialog.arguments = bundle
        customDialog.show(supportFragmentManager, "custom dialog")
    }

    fun viewImageFullScreen(view: View){
        if (currentUser == null) return
        val intent = Intent(this, FullScreenImage::class.java)
        if (selectedPhotoUri != null)
            intent.putExtra("image_uri", selectedPhotoUri)
        else
            intent.putExtra("image_url", currentUser!!.profileImageUrl)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun changePhotoClicked(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    fun saveChangesClicked(view: View) {
        if (currentUser == null) return

        if (usernameMyProfile.text.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }
        freezeGui(true)
        if (selectedPhotoUri != null)
            deleteOldProfileImage()
        else {
            updateUser(currentUser!!.profileImageUrl)
        }

    }

    private fun freezeGui(toFreeze: Boolean) {
        if (toFreeze) {
            progressBarMyProfile.visibility = View.VISIBLE
            createUserBtn.isClickable = false
            selectPhotoBtn.isClickable = false
            deleteAccountBtn.isClickable = false
        } else {
            progressBarMyProfile.visibility = View.GONE
            createUserBtn.isClickable = true
            selectPhotoBtn.isClickable = true
            deleteAccountBtn.isClickable = true
        }


    }

    private fun updateUser(profileImageUrl: String) {

        val updatedUser = User(
            currentUser!!.uid,
            usernameMyProfile.text.toString(),
            profileImageUrl,
            currentUser!!.token
        )
        val ref = FirebaseDatabase.getInstance().getReference("users/${currentUser!!.uid}")
        ref.setValue(updatedUser).addOnSuccessListener {
            Log.d(TAG, "username changed")
            Toast.makeText(
                this,
                "Changes Saved!",
                Toast.LENGTH_SHORT
            )
                .show()
            freezeGui(false)
            finish()

        }.addOnFailureListener {

            Log.d(TAG, "Failed to change username : ${it.message}")
            Toast.makeText(
                this,
                "Failed to change username: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()
            freezeGui(false)

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            Glide.with(applicationContext).load(selectedPhotoUri)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(changeProfileMyProfile)

        }
    }

    private fun deleteOldProfileImage() {
        val imageLocation = extractFilenameFromUrl()
        val oldRef =
            FirebaseStorage.getInstance().getReference("/images/$imageLocation")
        oldRef.delete().addOnSuccessListener {
            Log.d(TAG, "Successfully deleted old image")
            uploadImageToFirebaseStorage()
        }.addOnFailureListener {
            Log.d(TAG, "Failed to delete old image")
            freezeGui(false)

        }


    }

    private fun extractFilenameFromUrl(): String {
        return currentUser!!.profileImageUrl.substringAfter("%2F").substringBefore("?alt=")

    }

    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")
                updateUser(it.toString())
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload Image: ${it.message}")
            Toast.makeText(this, "Failed to upload Image: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            freezeGui(false)

        }
    }
}
