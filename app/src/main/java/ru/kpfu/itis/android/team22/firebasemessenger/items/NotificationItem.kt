package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemProfileNotificationBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.utils.NotificationSender

class NotificationItem(
    private val binding: ItemProfileNotificationBinding,
    private val glide: RequestManager,
    private val controller: NavController,
    private val userId: String,
    private val currentUser: FirebaseUser?,
    private val context: Context,
    private val dialog: Dialog,
) : RecyclerView.ViewHolder(binding.root) {
    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    fun onBind(user: User) {
        binding.run {
            userName.text =
                "${user.userName}${context.getString(R.string.added_you_to_friends)}"
            glide
                .load(user.profileImage)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivImage)
            setListeners(user.userId)
        }
    }

    private fun setListeners(userIdentifier: String) {
        with(binding) {
            val databaseReference = currentUser?.uid?.let { currentUserid ->
                getDatabaseReference(currentUserid)
            }
            val friendsList: ArrayList<String> = getFriendsList(databaseReference, userIdentifier)
            val notificationsList: ArrayList<String> =
                getNotificationsList(databaseReference, userIdentifier)

            ibAddFriend.setOnClickListener {
                if (!friendsList.contains(userIdentifier)) {
                    friendsList.add(userIdentifier)
                    NotificationSender.generateFriendAddingNotification(currentUser, userIdentifier)

                    databaseReference?.child("friendsList")?.setValue(friendsList)
                    ibAddFriend.visibility = View.GONE
                }
            }

            ibHide.setOnClickListener {
                databaseReference?.child("notificationsList")?.setValue(notificationsList)
            }

            root.setOnClickListener {
                val bundle: Bundle = bundleOf(userId to userIdentifier, "from" to "friends")
                controller.navigate(R.id.nav_from_container_to_user_profile, bundle)
                dialog.dismiss()
            }
        }
    }

    private fun getDatabaseReference(userIdentifier: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users").child(
            userIdentifier
        )
    }

    private fun getFriendsList(
        databaseReference: DatabaseReference?,
        userIdentifier: String
    ): ArrayList<String> {
        val friendsList: ArrayList<String> = ArrayList()
        databaseReference?.child("friendsList")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    friendsList.clear()
                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val id = dataSnapShot.getValue(String::class.java)
                        if (id != null) {
                            friendsList.add(id)
                        }
                    }
                    if (!friendsList.contains(userIdentifier)) {
                        binding.ibAddFriend.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
        return friendsList
    }

    private fun getNotificationsList(
        databaseReference: DatabaseReference?,
        userIdentifier: String
    ): ArrayList<String> {
        val notificationsList: ArrayList<String> = ArrayList()
        databaseReference?.child("notificationsList")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notificationsList.clear()
                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val id = dataSnapShot.getValue(String::class.java)
                        if (id != null && id != userIdentifier) {
                            notificationsList.add(id)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
        return notificationsList
    }
}
