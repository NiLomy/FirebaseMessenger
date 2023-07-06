package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
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
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var auth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileBinding.bind(view)
        setUpButtons()

        auth = Firebase.auth
        firebaseUser = auth?.currentUser
        databaseReference =
            firebaseUser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
            }

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                binding.run {
                    userName.text = user?.userName
                    val context = requireContext().applicationContext
                    Glide.with(context)
                        .load(user?.profileImage)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                        .into(ivImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setUpButtons() {
        binding.friendsButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_container_to_friends_list)
        }

        binding.fabSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_from_container_to_settings)
        }

        binding.btnLogOut.setOnClickListener {
            auth?.signOut()
            findNavController().navigate(R.id.nav_from_container_to_login)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
