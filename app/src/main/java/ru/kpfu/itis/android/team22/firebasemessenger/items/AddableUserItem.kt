package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.os.Bundle
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserToAddBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class AddableUserItem(
    private val binding: ItemUserToAddBinding,
    private val glide: RequestManager,
    private val controller: NavController,
    private val userId: String,
    private val currentUser: FirebaseUser?,
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

            val databaseReference =
                currentUser?.uid?.let { currentUserid ->
                    FirebaseDatabase.getInstance().getReference("Users").child(
                        currentUserid
                    )
                }
            val friendsList: ArrayList<String> = ArrayList()
            val notificationsList: ArrayList<String> = ArrayList()
            databaseReference?.child("friendsList")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        friendsList.clear()
                        for (dataSnapShot: DataSnapshot in snapshot.children) {
                            val id = dataSnapShot.getValue(String::class.java)
                            if (!friendsList.contains(id)) {
                                id?.let { it1 -> friendsList.add(it1) }
                            }
                        }
                        if (friendsList.contains(user.userId)) {
                            ib.setImageResource(R.drawable.ic_remove_user)
                        } else {
                            ib.setImageResource(R.drawable.ic_add_friend)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            val anotherUserDatabaseReference =
                FirebaseDatabase.getInstance().getReference("Users").child(
                    user.userId
                )
            anotherUserDatabaseReference.child("notificationsList")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        notificationsList.clear()
                        for (dataSnapShot: DataSnapshot in snapshot.children) {
                            val id = dataSnapShot.getValue(String::class.java)
                            if (id != null) {
                                notificationsList.add(id)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            ib.setOnClickListener {
                if (!friendsList.contains(user.userId)) {
                    friendsList.add(user.userId)
                    currentUser?.uid?.let { it1 -> notificationsList.add(it1) }
                } else {
                    friendsList.remove(user.userId)
                }
                databaseReference?.child("friendsList")?.setValue(friendsList)
                anotherUserDatabaseReference.child("notificationsList").setValue(notificationsList)
            }

            root.setOnClickListener {
                val bundle: Bundle = bundleOf(userId to user.userId, "from" to "friends")
                controller.navigate(R.id.nav_from_friends_searcher_to_user_profile, bundle)
            }
        }
    }
}
