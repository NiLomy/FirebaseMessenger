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
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.UserAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentMessagesBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class MessagesFragment : Fragment(R.layout.fragment_messages) {
    private var _binding: FragmentMessagesBinding? = null

    // TODO: после регистрации нового пользователя вылетает приложение, проблема с binding'ом
    private val binding get() = _binding!!
    private var adapter: UserAdapter? = null
    private var context: Context? = null
    private val userList: ArrayList<User> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessagesBinding.bind(view)
        context = requireContext().applicationContext

        getUsersList()
    }

    private fun initAdapter() {
        if (!isAdded || isDetached || activity == null) {
            // The fragment is not yet linked to the activity
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
        binding.rvUser.adapter = adapter
    }

    private fun getUsersList() {
        val firebase: FirebaseUser? = Firebase.auth.currentUser
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

//                val currentUser: User? = snapshot.getValue(User::class.java)
//                binding.run {
//                    val context = requireContext().applicationContext
//                    Glide.with(context)
//                        .load(currentUser?.profileImage)
//                        .placeholder(R.drawable.loading)
//                        .error(R.drawable.error)
//                        .into(currentUser?.profileImage)

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user?.userId != firebase?.uid) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}