package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
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
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.AddableUserAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentFriendsSearcherBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class FriendsSearcherFragment : Fragment(R.layout.fragment_friends_searcher) {
    private var _binding: FragmentFriendsSearcherBinding? = null
    private val binding get() = _binding!!
    private var adapter: AddableUserAdapter? = null
    private var context: Context? = null
    private val userList: ArrayList<User> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendsSearcherBinding.bind(view)
        context = requireContext().applicationContext

        setUpButtons()
        setUpSearchBar()
        getUsersList()
    }

    private fun setUpButtons() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_friends_searcher_to_friends_list)
        }
    }

    private fun getUsersList() {
        val firebase: FirebaseUser? = Firebase.auth.currentUser
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

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

    private fun initAdapter() {
        if (!isAdded || isDetached || activity == null) {
            // The fragment is not yet linked to the activity
            return
        }
        adapter = context?.let {
            AddableUserAdapter(
                list = userList,
                glide = Glide.with(this),
                onItemClick = { user ->
                    //TODO сделать так, чтобы переходил на профиль пользователя
    //                val bundle: Bundle = bundleOf("id" to user.userId)
    //                findNavController().navigate(R.id.nav_from_container_to_chat, bundle)
                },
                context = it,
            )
        }
        binding.rvUser.adapter = adapter
    }

    private fun setUpSearchBar() {
        binding.sv.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filter(it) }
                return false
            }
        })
    }

    private fun filter(input : String) {
        val filteredList : ArrayList<User> = ArrayList()

        for (item in userList) {
            if (item.userName.lowercase().trim().contains(input.lowercase().trim())) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            binding.tvNoResults.visibility = View.VISIBLE
        } else {
            binding.tvNoResults.visibility = View.GONE
        }
        adapter?.filter(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
