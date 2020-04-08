package com.yakirarie.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yakirarie.chatapp.activities.ChatLogActivity
import com.yakirarie.chatapp.adaptersItems.LatestMessageRow
import com.yakirarie.chatapp.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        val adapter = GroupAdapter<GroupieViewHolder>()
    }

    private val TAG = "HomeFragmentDebug"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewLatestMessages.adapter =
            adapter
        recyclerViewLatestMessages.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(context, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            if (!row.enableClick) return@setOnItemClickListener
            if (row.chatMessage.toId.size == 1) { // user to user msg
                if (row.chatPartnerUser == null)
                    if (row.chatMessage.fromId == FirebaseAuth.getInstance().uid)
                        removeDeletedUserInteractionsWithYou(row.chatMessage.toId[0])
                    else
                        removeDeletedUserInteractionsWithYou(row.chatMessage.fromId)
                else {
                    intent.putExtra(NewMessageFragment.USER_KEY, row.chatPartnerUser)
                    startActivity(intent)
                }
            } else { // group msg
                if (row.group == null)
                    removeDeletedGroupInteractionsWithYou(row.groupId!!)
                else {
                    intent.putExtra(NewMessageFragment.GROUP_KEY, row.group)
                    startActivity(intent)
                }
            }

        }


    }


    private fun removeDeletedGroupInteractionsWithYou(deletedId: String) {
        Toast.makeText(context, "This group no longer exists", Toast.LENGTH_SHORT).show()
        val uid = FirebaseAuth.getInstance().uid
        val refLatest =
            FirebaseDatabase.getInstance().getReference("latest-messages/$uid/$deletedId")
        refLatest.removeValue().addOnSuccessListener {
            Log.d(TAG, "group $deletedId deleted from your latest messages")

        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete latest-messages with group : ${it.message}")

        }
    }


    private fun removeDeletedUserInteractionsWithYou(deletedId: String) {
        Toast.makeText(context, "This user no longer exists", Toast.LENGTH_SHORT).show()
        val uid = FirebaseAuth.getInstance().uid
        val refLatest =
            FirebaseDatabase.getInstance().getReference("latest-messages/$uid/$deletedId")
        refLatest.removeValue().addOnSuccessListener {
            Log.d(TAG, "user $deletedId deleted from your latest messages")

        }.addOnFailureListener {

            Log.e(TAG, "Failed to delete latest-messages with deleted : ${it.message}")

        }

        val refMessages =
            FirebaseDatabase.getInstance().getReference("user-messages/$uid/$deletedId")
        refMessages.removeValue().addOnSuccessListener {
            Log.d(TAG, "user $deletedId deleted from your messages")

        }.addOnFailureListener {
            Log.e(TAG, "Failed to delete user-messages with deleted : ${it.message}")

        }


    }


}