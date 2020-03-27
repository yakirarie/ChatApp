package com.yakirarie.chatapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.custom_dialog.*

class CustomDialog : DialogFragment() {

    private val TAG = "CustomDialogDebug"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        custom_dialog_btn_no.setOnClickListener {
            dialog?.dismiss()
        }
        custom_dialog_btn_yes.setOnClickListener {
            val userToDelete = arguments?.getParcelable<User>("USER_TO_DELETE")
            if (userToDelete == null) {
                Toast.makeText(view.context, "Couldn't delete user, please try again.", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
            progressBarCustomDialog.visibility = View.VISIBLE
            custom_dialog_btn_no.isClickable = false
            custom_dialog_btn_yes.isClickable = false
            deleteUser(userToDelete)

        }
        custom_dialog_btn_approve.setOnClickListener {
            if (email_custom_dialog.text.isEmpty() || password_custom_dialog.text.isEmpty()){
                Toast.makeText(view.context, "Please fill all of the above", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBarCustomDialog.visibility = View.VISIBLE
            custom_dialog_btn_approve.isClickable = false
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email_custom_dialog.text.toString(), password_custom_dialog.text.toString()).addOnSuccessListener {
                deleteUserAuth()
            }.addOnFailureListener {
                Log.e(TAG, "Failed to login: ${it.message}")
                Toast.makeText(view.context, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
                custom_dialog_btn_approve.isClickable = true
                progressBarCustomDialog.visibility = View.INVISIBLE

            }



        }
    }

    private fun deleteUser(userToDelete: User?){
        val ref = FirebaseDatabase.getInstance().getReference("users/${userToDelete!!.uid}")
        ref.removeValue().addOnSuccessListener {
            Log.d(TAG, "user ${userToDelete.username} deleted from users table")
            deleteUserMessages(userToDelete)
        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete user-messages : ${it.message}")
            Toast.makeText(view?.context, "Failed to delete user-messages: ${it.message}", Toast.LENGTH_SHORT).show()
            progressBarCustomDialog.visibility = View.INVISIBLE
            dialog?.dismiss()

        }
    }

    private fun deleteUserMessages(userToDelete: User?){
        val ref = FirebaseDatabase.getInstance().getReference("user-messages/${userToDelete!!.uid}")
        ref.removeValue().addOnSuccessListener {
            Log.d(TAG, "user ${userToDelete.username}  messages deleted from users-messages table")
            deleteUserImage(userToDelete)
        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete username : ${it.message}")
            Toast.makeText(view?.context, "Failed to delete username: ${it.message}", Toast.LENGTH_SHORT).show()
            progressBarCustomDialog.visibility = View.INVISIBLE
            dialog?.dismiss()

        }
    }


    private fun deleteUserImage(userToDelete: User?){
        val imageLocation = extractFilenameFromUrl(userToDelete)
        val oldRef =
            FirebaseStorage.getInstance().getReference("/images/$imageLocation")
        oldRef.delete().addOnSuccessListener {
            Log.d(TAG, "Successfully deleted old image")
            progressBarCustomDialog.visibility = View.INVISIBLE
            custom_dialog_btn_no.visibility = View.GONE
            custom_dialog_btn_yes.visibility = View.GONE

            email_custom_dialog.visibility = View.VISIBLE
            password_custom_dialog.visibility = View.VISIBLE
            custom_dialog_btn_approve.visibility = View.VISIBLE
            custom_dialog_body.text = "Please log in again to continue this process"

        }.addOnFailureListener {
            Log.e(TAG, "Failed to delete old image")
            Toast.makeText(view?.context, "Failed to delete old image: ${it.message}", Toast.LENGTH_SHORT).show()
            progressBarCustomDialog.visibility = View.INVISIBLE
            dialog?.dismiss()

        }

    }

    private fun deleteUserAuth(){
        FirebaseAuth.getInstance().currentUser?.delete()?.addOnSuccessListener {
            Toast.makeText(view?.context, "User deleted successfully!", Toast.LENGTH_SHORT).show()
            progressBarCustomDialog.visibility = View.INVISIBLE
            FirebaseAuth.getInstance().signOut()
            dialog?.dismiss()
            activity?.finish()
        }?.addOnFailureListener {
            Log.d(TAG, "Failed to delete authentication")
            Toast.makeText(view?.context, "Failed to delete authentication: ${it.message}", Toast.LENGTH_SHORT).show()
            progressBarCustomDialog.visibility = View.INVISIBLE
            dialog?.dismiss()
        }


    }

    private fun extractFilenameFromUrl(userToDelete: User?): String {
        return userToDelete!!.profileImageUrl.substringAfter("%2F").substringBefore("?alt=")

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.custom_dialog, container, false)
    }

}