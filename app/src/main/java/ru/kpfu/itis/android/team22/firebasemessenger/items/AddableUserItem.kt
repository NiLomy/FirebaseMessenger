package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserToAddBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.utils.NotificationSender

class AddableUserItem(
    private val binding: ItemUserToAddBinding,
    private val glide: RequestManager,
    private val controller: NavController,
    private val userId: String,
    private val currentUser: FirebaseUser?,
    private val context: Context,
    private val rv: RecyclerView?
) : RecyclerView.ViewHolder(binding.root) {
    private var preferences: SharedPreferences? = null
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
                val lManager = rv?.layoutManager as LinearLayoutManager?
                val pos = lManager?.findFirstCompletelyVisibleItemPosition()

                preferences = context.getSharedPreferences(APP_POSITIONS, Context.MODE_PRIVATE)
                pos?.let {
                    preferences?.edit()
                        ?.putInt(PREF_ADD_FRIEND_POS, it)
                        ?.apply()
                }

                if (!friendsList.contains(userIdentifier)) {
                    friendsList.add(userIdentifier)
                    currentUser?.uid?.let { currentUserId -> notificationsList.add(currentUserId) }
                    NotificationSender.generateFriendAddingNotification(currentUser, userIdentifier)
                } else {
                    friendsList.remove(userIdentifier)
                    NotificationSender.generateFriendRemovingNotification(
                        currentUser,
                        userIdentifier
                    )
                }

                databaseReference?.child("friendsList")?.setValue(friendsList)
                anotherUserDatabaseReference.child("notificationsList").setValue(notificationsList)
            }

            root.setOnClickListener {
                val bundle: Bundle = bundleOf(userId to userIdentifier, "from" to "search")
                controller.navigate(R.id.nav_from_friends_searcher_to_user_profile, bundle)
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

    companion object {
        private const val APP_POSITIONS = "positions"
        private const val PREF_ADD_FRIEND_POS = "addFriendsPos"
    }
}
