package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var binding: FragmentSettingsBinding? = null
    private var currentUser: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        currentUser = FirebaseAuth.getInstance().currentUser

        databaseReference = currentUser?.uid?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
        }

        setClickListeners()
        initFields()
    }

    private fun initFields() {
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                binding?.run {
                    etNewName.setText(user?.userName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setClickListeners() {
        binding?.run {
            fabToContainer.setOnClickListener {
                findNavController().navigate(R.id.nav_from_settings_to_container)
            }

            applyChangesButton.setOnClickListener {
                binding?.applyChangesButton?.isEnabled = false
                currentUser?.run {
                    updateName(binding?.etNewName?.text.toString())
                }
                applyChangesButton.isEnabled = true
            }

            changeEmailPassword.setOnClickListener {
                val frag = SettingsDialogFragment()
                frag.show(parentFragmentManager, "data_change")
            }
        }
    }

    private fun updateName(
        newName: String
    ) {
        val hashMap = readUserInfo(databaseReference)

        if (newName.isEmpty()) {
            makeToast("Username must be non-empty.")
        } else if (newName.length > 20) {
            makeToast("Username is too long. Try shorter.")
        } else {
            hashMap["userName"] = newName
            databaseReference?.updateChildren(hashMap as Map<String, Any>)

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser?.updateProfile(profileUpdates)

            makeToast("Success!")
            findNavController().navigate(R.id.nav_from_settings_to_container)
        }
    }

    private fun readUserInfo(databaseReference: DatabaseReference?): HashMap<String, String> {
        val hashMap: HashMap<String, String> = HashMap()
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                user?.run {
                    hashMap["profileImage"] = this.profileImage
                    hashMap["userId"] = this.userId
                    hashMap["userName"] = this.userName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                makeToast(error.message)
            }
        })
        return hashMap
    }

    private fun makeToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
