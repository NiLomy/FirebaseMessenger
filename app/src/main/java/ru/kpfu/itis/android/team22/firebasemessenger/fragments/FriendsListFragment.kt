package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var binding: FragmentFriendsListBinding? = null
    private var adapter: ChattableUserAdapter? = null
    private var context: Context? = null
    private var searchText: String? = null
    private val userList: ArrayList<User> = ArrayList()

    private var rvPos : Int? = null
    private var preferences : SharedPreferences? = null
    private val APP_POSITIONS = "positions"
    private val PREF_FRIEND_LST_POS = "friendsListPos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = activity?.getSharedPreferences(APP_POSITIONS, Context.MODE_PRIVATE)
        rvPos = preferences?.getInt(PREF_FRIEND_LST_POS, 0)
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = binding?.rvUser?.layoutManager as LinearLayoutManager
        val pos = layoutManager.findFirstCompletelyVisibleItemPosition()
        preferences?.edit()
            ?.putInt(PREF_FRIEND_LST_POS, pos)
            ?.apply()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFriendsListBinding.bind(view)
        context = requireContext()

        setUpButtons()
        setUpSearchBar()
        getFriendsList()
    }

    private fun setUpButtons() {
        binding?.run {
            backButton.setOnClickListener {
                findNavController().navigate(R.id.nav_from_friends_list_to_container)
            }

            buttonFindFriends.setOnClickListener {
                findNavController().navigate(R.id.nav_from_friends_list_to_friends_searcher)
            }
        }
    }

    private fun setUpSearchBar() {
        binding?.sv?.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchText = query
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchText = newText
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

    private fun getFriendsList() {
        val currentUser: FirebaseUser? = Firebase.auth.currentUser
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")
        val currentUserDatabaseReference: DatabaseReference? =
            currentUser?.uid?.let {
                databaseReference.child(it).child("friendsList")
            }
        val friendsList: ArrayList<String> = getFriendsList(currentUserDatabaseReference)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (friendsList.contains(user?.userId)) {
                        if (user != null) {
                            userList.add(user)
                        }
                    }
                }
                initAdapter()
                rvPos?.let {binding?.rvUser?.layoutManager?.scrollToPosition(it)}
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFriendsList(databaseReference: DatabaseReference?): ArrayList<String> {
        val friendsList: ArrayList<String> = ArrayList()
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendsList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val id = dataSnapShot.getValue(String::class.java)
                    if (id != null) {
                        friendsList.add(id)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        return friendsList
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
        binding?.rvUser?.adapter = adapter
        binding?.sv?.setQuery(searchText, false)
        searchText?.let { filter(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
