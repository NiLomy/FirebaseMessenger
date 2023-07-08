package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
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
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentUserProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private var auth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null
    private var userID: String? = null
    private var destination: String? = null

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
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.tvUserName.text = user?.userName
                loadImage(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadImage(user: User?) {
        val context = requireContext().applicationContext
        Glide.with(context)
            .load(user?.profileImage)
            .transform(CenterCrop())
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(binding.ivImage)
    }

    private fun setOnClickListeners() {
        binding.run {
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
                    userID?.let { userId -> friendsList.add(userId) }
                    currentUser?.uid?.let { it1 -> notificationsList.add(it1) }
                } else {
                    userID?.let { userId -> friendsList.remove(userId) }
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
                        binding.ibFriend.setImageResource(R.drawable.ic_remove_user)
                    } else {
                        binding.ibFriend.setImageResource(R.drawable.ic_add_friend)
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
