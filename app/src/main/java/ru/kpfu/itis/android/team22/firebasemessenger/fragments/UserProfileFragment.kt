package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentUserProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.utils.IconUploader
import ru.kpfu.itis.android.team22.firebasemessenger.utils.NotificationSender

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private var binding: FragmentUserProfileBinding? = null
    private var auth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null
    private var userID: String? = null
    private var destination: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)
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
            override fun onDataChange(snapshot: DataSnapshot) {
                val context = requireContext().applicationContext
                val user = snapshot.getValue(User::class.java)
                binding?.ivImage?.let { IconUploader.loadDrawableImage(context, user, it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setOnClickListeners() {
        binding?.run {
            val bundle = Bundle()
            bundle.putString("id", userID)

            val currentUser: FirebaseUser? = auth?.currentUser
            val databaseReference =
                currentUser?.uid?.let { currentUserid -> getDatabaseReference(currentUserid) }
            val anotherUserDatabaseReference = userID?.let { getDatabaseReference(it) }

            val friendsList: ArrayList<String> = getFriendsList(databaseReference)
            val notificationsList: ArrayList<String> =
                getNotificationsList(anotherUserDatabaseReference)

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

            ibFriend.setOnClickListener {
                if (!friendsList.contains(userID)) {
                    currentUser?.uid?.let { it1 -> notificationsList.add(it1) }
                    userID?.let { userId -> friendsList.add(userId) }
                    userID?.let { userId ->
                        NotificationSender.generateFriendAddingNotification(currentUser, userId)
                    }
                } else {
                    userID?.let { userId -> friendsList.remove(userId) }
                    userID?.let { userId ->
                        NotificationSender.generateFriendRemovingNotification(currentUser, userId)
                    }
                }

                databaseReference?.child("friendsList")?.setValue(friendsList)
                anotherUserDatabaseReference?.child("notificationsList")
                    ?.setValue(notificationsList)
            }
        }
    }

    private fun getDatabaseReference(userIdentifier: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users").child(userIdentifier)
    }

    private fun getFriendsList(databaseReference: DatabaseReference?): ArrayList<String> {
        val friendsList: ArrayList<String> = ArrayList()
        databaseReference?.child("friendsList")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fillListWithData(friendsList, snapshot)

                    if (friendsList.contains(userID)) {
                        binding?.ibFriend?.setImageResource(R.drawable.ic_remove_user)
                    } else {
                        binding?.ibFriend?.setImageResource(R.drawable.ic_add_friend)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        return friendsList
    }

    private fun getNotificationsList(databaseReference: DatabaseReference?): ArrayList<String> {
        val notificationsList: ArrayList<String> = ArrayList()
        databaseReference?.child("notificationsList")
            ?.addValueEventListener(object : ValueEventListener {
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
