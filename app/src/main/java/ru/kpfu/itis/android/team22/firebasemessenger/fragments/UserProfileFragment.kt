package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentUserProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.NotificationData
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.PushNotification
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.RetrofitInstance

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference? = null

    private var userID: String? = null

    private var destination: String? = null
    private var auth: FirebaseAuth? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)
        auth = Firebase.auth

        userID = arguments?.getString(getString(R.string.user_id_tag))
        destination = arguments?.getString("from")


        databaseReference =
            userID?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }

        databaseReference?.let { loadUserInfo(it) }

        setOnClickListeners()
    }

    private fun loadUserInfo(databaseReference: DatabaseReference) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.tvUserName.text = user!!.userName
                val context = requireContext().applicationContext
                Glide.with(context)
                    .load(user.profileImage)
                    .transform(CenterCrop())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(binding.ivImage)

            }
        })
    }

    private fun setOnClickListeners() {
        binding.run {
            val bundle = Bundle()
            bundle.putString("id", userID)

            fabBack.setOnClickListener {
                when (destination) {
                    "chat" -> {
                        findNavController().navigate(R.id.nav_from_user_profile_to_chat, bundle)
                    }

                    "friends" -> findNavController().navigate(R.id.nav_from_user_profile_to_friends_list)
                }
            }

            ibMessage.setOnClickListener {
                findNavController().navigate(R.id.nav_from_user_profile_to_chat, bundle)
            }

            val currentUser: FirebaseUser? = auth?.currentUser
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
                            if (!friendsList.contains(id)) {
                                id?.let { it1 -> friendsList.add(it1) }
                            }
                        }
                        if (friendsList.contains(userID)) {
                            ibFriend.setImageResource(R.drawable.ic_remove_user)
                        } else {
                            ibFriend.setImageResource(R.drawable.ic_add_friend)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            val notificationsList: ArrayList<String> = ArrayList()
            val anotherUserDatabaseReference = userID?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
            }
            anotherUserDatabaseReference?.child("notificationsList")
                ?.addValueEventListener(object : ValueEventListener {
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

            ibFriend.setOnClickListener {

                if (!friendsList.contains(userID)) {
                    userID?.let { userId -> friendsList.add(userId) }
                    currentUser?.uid?.let { it1 -> notificationsList.add(it1) }

                    PushNotification(
                        NotificationData("You have a new friend!", currentUser!!.displayName!! + " just added you to his friends."),
                        "/topics/friend_$userID"
                    )
                        .also {
                            sendNotification(it)
                        }
                } else {
                    userID?.let { userId -> friendsList.remove(userId) }
                    PushNotification(
                        NotificationData("Bad news...", currentUser!!.displayName!! + " just removed you from his friends."),
                        "/topics/friend_$userID"
                    )
                        .also {
                            sendNotification(it)
                        }
                }


                databaseReference?.child("friendsList")?.setValue(friendsList)
                anotherUserDatabaseReference?.child("notificationsList")
                    ?.setValue(notificationsList)
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