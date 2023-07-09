package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.UserAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentMessagesBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class MessagesFragment : Fragment(R.layout.fragment_messages) {
    private var binding: FragmentMessagesBinding? = null
    private var context: Context? = null
    // TODO: после регистрации нового пользователя вылетает приложение, проблема с binding'ом
    private var adapter: UserAdapter? = null
    private val userList: ArrayList<User> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMessagesBinding.bind(view)
        context = requireContext().applicationContext

        setUpSearchBar()
        getUsersList()
    }

    private fun setUpSearchBar() {
        binding?.sv?.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filter(it) }
                return false
            }
        })
    }

    private fun filter(input: String) {
        val filteredList: ArrayList<User> = ArrayList()

        for (item in userList) {
            if (item.userName.lowercase().trim().contains(input.lowercase().trim())) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            binding?.tvNoResults?.visibility = View.VISIBLE
        } else {
            binding?.tvNoResults?.visibility = View.GONE
        }
        adapter?.filter(filteredList)
    }

    private fun getUsersList() {
        val currentUser: FirebaseUser? = Firebase.auth.currentUser
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")
        val currentUserDatabaseReference: DatabaseReference? =
            currentUser?.uid?.let {
                databaseReference.child(it).child("chatsList")
            }
        val chatsList: ArrayList<String> = getUsersToChatList(currentUserDatabaseReference)
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/msg_${currentUser?.uid}")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (chatsList.contains(user?.userId)) {
                        if (user != null) {
                            userList.add(user)
                        }
                    }
                }
                initAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUsersToChatList(databaseReference: DatabaseReference?): ArrayList<String> {
        val chatsList: ArrayList<String> = ArrayList()
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val id = dataSnapShot.getValue(String::class.java)
                    if (id != null) {
                        chatsList.add(id)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        return chatsList
    }

    private fun initAdapter() {
        if (!isAdded || isDetached || activity == null) {
            return
        }

        adapter = UserAdapter(
            list = userList,
            glide = Glide.with(this),
            onItemClick = { user ->
                val bundle: Bundle = bundleOf("id" to user.userId)
                findNavController().navigate(R.id.nav_from_container_to_chat, bundle)
            }
        )
        binding?.rvUser?.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
