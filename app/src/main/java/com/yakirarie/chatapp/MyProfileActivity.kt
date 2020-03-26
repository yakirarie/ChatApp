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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_my_profile.changeProfileMyProfile
import kotlinx.android.synthetic.main.activity_my_profile.usernameMyProfile
import java.util.*

class MyProfileActivity : AppCompatActivity() {

    private val TAG = "MyProfileActivityDebug"
    lateinit var currentUser: User
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
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
                usernameMyProfile.setText(currentUser.username)
                Picasso.get().load(currentUser.profileImageUrl).into(changeProfileMyProfile)

            }

        })

    }

    fun changePhotoClicked(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    fun saveChangesClicked(view: View) {
        if (usernameMyProfile.text.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }
        progressBarMyProfile.visibility = View.VISIBLE
        if (selectedPhotoUri != null)
            deleteOldProfileImage()
        else{
            updateUser(currentUser.profileImageUrl)
        }

    }

    private fun updateUser(profileImageUrl: String) {

        val updatedUser = User(
            currentUser.uid,
            usernameMyProfile.text.toString(),
            profileImageUrl,
            currentUser.token
        )
        val ref = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}")
        ref.setValue(updatedUser).addOnSuccessListener {
            Log.d(TAG, "username changed")
            Toast.makeText(
                this,
                "Changes Saved!",
                Toast.LENGTH_SHORT
            )
                .show()
            progressBarMyProfile.visibility = View.GONE
            finish()

        }.addOnFailureListener {

            Log.d(TAG, "Failed to change username : ${it.message}")
            Toast.makeText(
                this,
                "Failed to change username: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()
            progressBarMyProfile.visibility = View.GONE

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            changeProfileMyProfile.setImageBitmap(bitmap)

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
            progressBarMyProfile.visibility = View.GONE

        }


    }

    private fun extractFilenameFromUrl(): String {
        return currentUser.profileImageUrl.substringAfter("%2F").substringBefore("?alt=")

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
            progressBarMyProfile.visibility = View.GONE

        }
    }
}
