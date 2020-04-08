package com.yakirarie.chatapp.dialogs

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.activities.MainActivity
import com.yakirarie.chatapp.classObjects.Group
import kotlinx.android.synthetic.main.delete_group_dialog.*

class DeleteGroupDialog: DialogFragment() {

    private val TAG = "DeleteGroupDialogDebug"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        delete_group_btn_no.setOnClickListener {
            dialog?.dismiss()
        }

        delete_group_btn_yes.setOnClickListener {
            val groupToDelete = arguments?.getParcelable<Group>("GROUP_TO_DELETE")
            if (groupToDelete == null) {
                Toast.makeText(
                    view.context,
                    "Couldn't delete group, please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                dialog?.dismiss()
            }
            else{
                freezeGui(true)
                deleteGroup(groupToDelete)

            }
        }
    }

    private fun freezeGui(toFreeze: Boolean) {
        if (toFreeze) {
            progressBarDeleteGroup.visibility = View.VISIBLE
            delete_group_btn_yes.isClickable = false
            delete_group_btn_no.isClickable = false
        } else {
            progressBarDeleteGroup.visibility = View.INVISIBLE
            delete_group_btn_yes.isClickable = true
            delete_group_btn_no.isClickable = true

        }
    }

    private fun deleteGroup(groupToDelete: Group) {
        val ref = FirebaseDatabase.getInstance().getReference("users/${groupToDelete.uid}")
        ref.removeValue().addOnSuccessListener {
            Log.d(TAG, "group ${groupToDelete.groupName} deleted from users table")
            deleteGroupMessages(groupToDelete)
        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete group : ${it.message}")
            deleteGroupMessages(groupToDelete)

        }
    }

    private fun deleteGroupMessages(groupToDelete: Group) {
        val ref = FirebaseDatabase.getInstance().getReference("group-messages/${groupToDelete.uid}")
        ref.removeValue().addOnSuccessListener {
            Log.d(TAG, "Group ${groupToDelete.groupName} messages deleted from group-messages table")
            deleteGroupImage(groupToDelete)
        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete group-messages : ${it.message}")
            deleteGroupImage(groupToDelete)


        }
    }


    private fun deleteGroupImage(groupToDelete: Group) {
        val imageLocation = extractFilenameFromUrl(groupToDelete)
        val oldRef =
            FirebaseStorage.getInstance().getReference("/Groups Profiles/$imageLocation")
        oldRef.delete().addOnSuccessListener {
            Log.d(TAG, "Successfully deleted old image")
            deleteGroupMediaMessages(groupToDelete)
        }.addOnFailureListener {
            deleteGroupMediaMessages(groupToDelete)
            Log.e(TAG, "Failed to delete old image: ${it.message} $imageLocation")
        }

    }

    private fun deleteGroupMediaMessages(groupToDelete: Group) {
        val oldRef =
            FirebaseStorage.getInstance().getReference("/Groups Media Messages/${groupToDelete.uid}")
        oldRef.listAll().addOnSuccessListener {
            it.items.forEach { storageReference ->
                storageReference.delete().addOnSuccessListener {
                    Log.d(TAG, "Successfully deleted group media message ${storageReference.name}")
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to deleted group media message ${storageReference.name}\n${it.message}")
                }

            }
            Toast.makeText(view!!.context, "Group deleted successfully!", Toast.LENGTH_SHORT)
                .show()
            freezeGui(false)
            dialog?.dismiss()
            val intent = Intent(view!!.context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            Log.e(TAG, "Failed to deleted messages images directory ${it.message}")
            freezeGui(false)
            dialog?.dismiss()
            activity?.finish()
        }


    }

    private fun extractFilenameFromUrl(groupToDelete: Group?): String {
        return groupToDelete!!.groupImageUrl.substringAfter("%2F").substringBefore("?alt=")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.delete_group_dialog, container, false)
    }
}