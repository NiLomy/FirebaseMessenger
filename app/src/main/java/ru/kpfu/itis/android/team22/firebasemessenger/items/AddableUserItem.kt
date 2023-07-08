package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserToAddBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.NotificationData
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.PushNotification
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.RetrofitInstance

class AddableUserItem(
    private val binding: ItemUserToAddBinding,
    private val glide: RequestManager,
    private val controller: NavController,
    private val userId: String,
    private val currentUser: FirebaseUser?,
    private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {
    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    fun onBind(user: User) {
        binding.run {
            userName.text = user.userName
            glide
                .load(user.profileImage)
                .transform(CenterCrop())
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
            val anotherUserDatabaseReference = getDatabaseReference(userIdentifier)

            val friendsList: ArrayList<String> = getFriendsList(databaseReference, userIdentifier)
            val notificationsList: ArrayList<String> =
                getNotificationsList(anotherUserDatabaseReference)

            ib.setOnClickListener {
                if (!friendsList.contains(userIdentifier)) {
                    friendsList.add(userIdentifier)
                    currentUser?.uid?.let { currentUserId -> notificationsList.add(currentUserId) }
                    PushNotification(
                        NotificationData("You have a new friend!", currentUser!!.displayName!! + " just added you to his friends."),
                        "/topics/friend_${user.userId}"
                    )
                        .also {
                            sendNotification(it)
                        }
                } else {
                    friendsList.remove(userIdentifier)
                    PushNotification(
                        NotificationData("Bad news...", currentUser!!.displayName!! + " just removed you from his friends."),
                        "/topics/friend_${user.userId}"
                    )
                        .also {
                            sendNotification(it)
                        }
                }

                databaseReference?.child("friendsList")?.setValue(friendsList)
                anotherUserDatabaseReference.child("notificationsList").setValue(notificationsList)
            }

            root.setOnClickListener {
                val bundle: Bundle = bundleOf(userId to userIdentifier, "from" to "friends")
                controller.navigate(R.id.nav_from_friends_searcher_to_user_profile, bundle)
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
                    fillListWithData(friendsList, snapshot)
                    if (friendsList.contains(userIdentifier)) {
                        binding.ib.setImageResource(R.drawable.ic_remove_user)
                    } else {
                        binding.ib.setImageResource(R.drawable.ic_add_friend)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
        return friendsList
    }

    private fun getNotificationsList(databaseReference: DatabaseReference): ArrayList<String> {
        val notificationsList: ArrayList<String> = ArrayList()
        databaseReference.child("notificationsList")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fillListWithData(notificationsList, snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
        return notificationsList
    }

    private fun fillListWithData(list: ArrayList<String>, snapshot: DataSnapshot) {
        list.clear()
        for (dataSnapShot: DataSnapshot in snapshot.children) {
            val id = dataSnapShot.getValue(String::class.java)
            if (id != null) {
                list.add(id)
            }
        }
    }
}
