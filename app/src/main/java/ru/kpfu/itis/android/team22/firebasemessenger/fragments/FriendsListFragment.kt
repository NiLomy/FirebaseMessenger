package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
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
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.ChattableUserAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentFriendsListBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class FriendsListFragment : Fragment(R.layout.fragment_friends_list) {
    private var _binding: FragmentFriendsListBinding? = null
    private val binding get() = _binding!!
    private var adapter: ChattableUserAdapter? = null
    private var context: Context? = null
    private val userList: ArrayList<User> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendsListBinding.bind(view)
        context = requireContext().applicationContext

        setUpButtons()
        setUpSearchBar()
        getFriendsList()
    }

    private fun setUpButtons() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_friends_list_to_container)
        }

        binding.buttonFindFriends.setOnClickListener {
            findNavController().navigate(R.id.nav_from_friends_list_to_friends_searcher)
        }
    }

    private fun getFriendsList() {
        val firebase: FirebaseUser? = Firebase.auth.currentUser
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")
        val currentUserDatabaseReference: DatabaseReference? =
            firebase?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it).child("friendsList")
            }
        val list: ArrayList<String> = ArrayList()
        currentUserDatabaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val id = dataSnapShot.getValue(String::class.java)
                    id?.let { list.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (list.contains(user?.userId)) {
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

        adapter = ChattableUserAdapter(
            list = userList,
            glide = Glide.with(this),
            controller = findNavController(),
            userId = getString(R.string.user_id_tag)
        )
        binding.rvUser.adapter = adapter
    }

    private fun setUpSearchBar() {
        binding.sv.setOnQueryTextListener(object :
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
