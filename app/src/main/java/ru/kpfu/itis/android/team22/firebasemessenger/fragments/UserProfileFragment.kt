package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentUserProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class UserProfileFragment : Fragment(R.layout.fragment_user_profile){
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference? = null

    private var userID : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        userID = arguments?.getString(getString(R.string.user_id_tag))


        databaseReference =
            userID?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }

        databaseReference?.let {loadUserInfo(it)}

        binding.fabToChat.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", userID)
            findNavController().navigate(R.id.nav_from_user_profile_to_chat, bundle)
        }
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
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(binding.ivImage)

            }
        })
    }
}