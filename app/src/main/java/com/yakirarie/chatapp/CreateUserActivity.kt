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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    private val TAG = "CreateUserActivityDebug"
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
    }


    fun createUserClicked(view: View) {
        val email = createEmailText.text.toString()
        val password = createPasswordText.text.toString()
        val username = usernameMyProfile.text.toString()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill all of the above", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please add a profile image", Toast.LENGTH_SHORT)
                .show()
            return
        }
        progressBar.visibility = View.VISIBLE
        selectPhotoBtn.isClickable = false
        createUserBtn.isClickable = false
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(TAG, "Successfully created user with uid: ${it.result?.user?.uid}")
                uploadImageToFirebaseStorage()

            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
                progressBar.visibility = View.GONE
                selectPhotoBtn.isClickable = true
                createUserBtn.isClickable = true

            }

    }

    fun selectPhotoClicked(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            changeProfileMyProfile.setImageBitmap(bitmap)
            defaultProfileImageView.alpha = 0f

        }
    }

    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")
                getToken(it.toString())

            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload Image: ${it.message}")
            Toast.makeText(this, "Failed to upload Image: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            progressBar.visibility = View.GONE
            selectPhotoBtn.isClickable = true
            createUserBtn.isClickable = true

        }

    }

    private fun getToken(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            saveUserToFirebaseDataBase(profileImageUrl, it.token)
        }.addOnFailureListener {
            Log.d(TAG, "Unable to get token: ${it.message}")
            Toast.makeText(
                this,
                "Unable to get token: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()
            progressBar.visibility = View.GONE
            selectPhotoBtn.isClickable = true
            createUserBtn.isClickable = true

        }

    }


    private fun saveUserToFirebaseDataBase(profileImageUrl: String, token:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, usernameMyProfile.text.toString(), profileImageUrl, token)
        ref.setValue(user).addOnSuccessListener {
            Log.d(TAG, "Successfully saved user to Firestore!")
            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT)
                .show()
            progressBar.visibility = View.GONE

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }.addOnFailureListener {
            Log.d(TAG, "Failed to save user to Firestore: ${it.message}")
            Toast.makeText(
                this,
                "Failed to save user to database: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()
            progressBar.visibility = View.GONE
            selectPhotoBtn.isClickable = true
            createUserBtn.isClickable = true

        }
    }



}

