package com.yakirarie.chatapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.yakirarie.chatapp.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import java.util.*

class MyProfileFragment : Fragment() {

    private val TAG = "MyProfileActivityDebug"
    private var currentUser: User? = null
    private var selectedPhotoUri: Uri? = null
    private val statuses = arrayListOf(
        "\uD83D\uDC4C Available \uD83D\uDC4C",
        "\uD83D\uDD0C Low Battery \uD83D\uDD0C",
        "\uD83D\uDEA8 Emergencies Only \uD83D\uDEA8",
        "\uD83D\uDD15 Busy \uD83D\uDD15"
    )
    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = MainActivity.currentUser
        val arrayAdapter = ArrayAdapter(context!!,
            R.layout.spinner_item, statuses)
        spinnerStatus.adapter = arrayAdapter
        fetchCurrentUser()
        selectPhotoBtn.setOnClickListener {
            changePhotoClicked()
        }
        deleteAccountBtn.setOnClickListener {
            deleteAccountClicked()
        }
        createUserBtn.setOnClickListener {
            saveChangesClicked()
        }
        changeProfileMyProfile.setOnClickListener {
            viewImageFullScreen()
        }
    }

    private fun fetchCurrentUser() {
        if(currentUser == null) return
        usernameMyProfile.setText(currentUser!!.username)
        Glide.with(context!!).load(currentUser!!.profileImageUrl)
            .placeholder(R.drawable.ic_loading_sign)
            .error(R.drawable.ic_error_sign).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).into(changeProfileMyProfile)
        Log.d(TAG, statuses.indexOf(currentUser!!.status).toString())
        spinnerStatus.setSelection(statuses.indexOf(currentUser!!.status))

    }

    private fun deleteAccountClicked() {
        if (currentUser == null) return
        val bundle = Bundle()
        bundle.putParcelable("USER_TO_DELETE", currentUser)
        val customDialog = DeleteUserDialog()
        customDialog.arguments = bundle
        customDialog.show(activity!!.supportFragmentManager, "custom dialog")
    }

    private fun viewImageFullScreen() {
        if (currentUser == null) return
        val intent = Intent(context, FullScreenMedia::class.java)
        if (selectedPhotoUri != null) {
            intent.putExtra("image_uri", selectedPhotoUri)
            intent.putExtra("media_type", "image")
        } else {
            intent.putExtra("image_url", currentUser!!.profileImageUrl)
            intent.putExtra("media_type", "image")
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun changePhotoClicked() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    private fun saveChangesClicked() {
        if (currentUser == null) return

        if (usernameMyProfile.text.isEmpty()) {
            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }
        loadingDialog.show(activity!!.supportFragmentManager, "loading")
        if (selectedPhotoUri != null)
            deleteOldProfileImage()
        else {
            updateUser(currentUser!!.profileImageUrl)
        }

    }


    private fun updateUser(profileImageUrl: String) {

        val updatedUser = User(
            currentUser!!.uid,
            usernameMyProfile.text.toString(),
            profileImageUrl,
            currentUser!!.token,
            spinnerStatus.selectedItem.toString()
        )
        val ref = FirebaseDatabase.getInstance().getReference("users/${currentUser!!.uid}")
        ref.setValue(updatedUser).addOnSuccessListener {
            Log.d(TAG, "username changed")
            Toast.makeText(
                context,
                "Changes Saved!",
                Toast.LENGTH_SHORT
            )
                .show()
            MainActivity.currentUser = updatedUser
            loadingDialog.dismiss()

        }.addOnFailureListener {

            Log.d(TAG, "Failed to change username : ${it.message}")
            Toast.makeText(
                context,
                "Failed to change username: ${it.message}",
                Toast.LENGTH_SHORT
            )
                .show()
            loadingDialog.dismiss()

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            Glide.with(context!!).load(selectedPhotoUri)
                .placeholder(R.drawable.ic_loading_sign)
                .error(R.drawable.ic_error_sign).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).into(changeProfileMyProfile)

        }
    }

    private fun deleteOldProfileImage() {
        val imageLocation = extractFilenameFromUrl()
        val oldRef =
            FirebaseStorage.getInstance().getReference("/Profile Images/$imageLocation")
        oldRef.delete().addOnSuccessListener {
            Log.d(TAG, "Successfully deleted old image")
            uploadImageToFirebaseStorage()
        }.addOnFailureListener {
            Log.d(TAG, "Failed to delete old image")
            loadingDialog.dismiss()

        }


    }

    private fun extractFilenameFromUrl(): String {
        return currentUser!!.profileImageUrl.substringAfter("%2F").substringBefore("?alt=")

    }

    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/Profile Images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")
                updateUser(it.toString())
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload Image: ${it.message}")
            Toast.makeText(context, "Failed to upload Image: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            loadingDialog.dismiss()

        }
    }
}