package com.yakirarie.chatapp.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.classObjects.User
import com.yakirarie.chatapp.dialogs.LoadingDialog
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    private val TAG = "CreateUserActivityDebug"
    private var selectedPhotoUri: Uri? = null
    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        supportActionBar?.title = "Create User"

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
        loadingDialog.show(supportFragmentManager, "loading")
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
                loadingDialog.dismiss()

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
            Glide.with(this).load(selectedPhotoUri)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(changeProfileCreateUser)
            defaultProfileImageView.alpha = 0f

        }
    }

    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/Profile Images/$filename")

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
            loadingDialog.dismiss()

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
            loadingDialog.dismiss()

        }

    }


    private fun saveUserToFirebaseDataBase(profileImageUrl: String, token:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            usernameMyProfile.text.toString(),
            profileImageUrl,
            token
        )
        ref.setValue(user).addOnSuccessListener {
            Log.d(TAG, "Successfully saved user to Firestore!")
            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT)
                .show()
            loadingDialog.dismiss()

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
            loadingDialog.dismiss()


        }
    }



}

