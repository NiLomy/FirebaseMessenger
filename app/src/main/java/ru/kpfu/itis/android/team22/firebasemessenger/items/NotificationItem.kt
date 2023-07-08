package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemProfileNotificationBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.NotificationData
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.PushNotification
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.RetrofitInstance

class NotificationItem(
    private val binding: ItemProfileNotificationBinding,
    private val glide: RequestManager,
    private val controller: NavController,
    private val userId: String,
    private val dialog: Dialog,
    private val currentUser: FirebaseUser?,
) : RecyclerView.ViewHolder(binding.root) {

    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    fun onBind(user: User) {
        binding.run {
            userName.text = "${user.userName} added you to friends "

            glide
                .load(user.profileImage)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivImage)

            val databaseReference =
                currentUser?.uid?.let { currentUserid ->
                    FirebaseDatabase.getInstance().getReference("Users").child(
                        currentUserid
                    )
                }

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
                        if (!friendsList.contains(user.userId)) {
                            ibAddFriend.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            ibAddFriend.setOnClickListener {
                if (!friendsList.contains(user.userId)) {
                    PushNotification(
                        NotificationData("You have a new friend!", currentUser!!.displayName!! + " just added you to his friends."),
                        "/topics/friend_${user.userId}"
                    )
                        .also {
                            sendNotification(it)
                        }
                    friendsList.add(user.userId)
                    databaseReference?.child("friendsList")?.setValue(friendsList)
                    ibAddFriend.visibility = View.GONE
                }
            }

            val notificationsList: ArrayList<String> = ArrayList()
            databaseReference?.child("notificationsList")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        notificationsList.clear()
                        for (dataSnapShot: DataSnapshot in snapshot.children) {
                            val id = dataSnapShot.getValue(String::class.java)
                            if (id != null && id != user.userId) {
                                notificationsList.add(id)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })


            ibHide.setOnClickListener {
                databaseReference?.child("notificationsList")?.setValue(notificationsList)
            }

            root.setOnClickListener {
                val bundle: Bundle = bundleOf(userId to user.userId, "from" to "friends")
                controller.navigate(R.id.nav_from_container_to_user_profile, bundle)
                dialog.dismiss()
            }
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d("PUSH", "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e("PUSH", response.errorBody()!!.string())
                }
            } catch (e: Exception) {
                Log.e("PUSH", e.toString())
            }
        }
}
