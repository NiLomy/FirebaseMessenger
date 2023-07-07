package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var binding: FragmentSettingsBinding? = null
    // Так нужно, иначе будут баги с вылетом
//    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference? = null
    private var currUser: FirebaseUser? = null
    private var profilePictureUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    // TODO ("Добавить смену параля через FirebaseUser.updatePassword()?)
    // TODO ("Обновлять userName у FirebaseUser")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        currUser = FirebaseAuth.getInstance().currentUser

        databaseReference = currUser?.uid?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
        }

        setClickListeners()
        initFields()
    }

    private fun initFields() {
        databaseReference = currUser?.uid?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
        }

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)

                binding?.run {
                    etNewName.setText(user?.userName)

                    if (isAdded) {
                        val context = requireContext().applicationContext
                        Glide.with(context)
                            .load(user?.profileImage)
                            .transform(CenterCrop())
                            .placeholder(R.drawable.loading)
                            .error(R.drawable.error)
                            .into(ivProfilePicture)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setClickListeners() {
        binding?.ibGallery?.setOnClickListener {
            binding?.ivProfilePicture!!.isEnabled = false
            openGallery()
            binding?.ivProfilePicture!!.isEnabled = true
        }

        binding?.fabToContainer?.setOnClickListener {
            findNavController().navigate(R.id.nav_from_settings_to_container)
        }

        binding?.applyChangesButton?.setOnClickListener {
            binding?.applyChangesButton!!.isEnabled = false
            currUser?.run {
                if (profilePictureUri != null) {
                    updateNameAndImage(
                        binding?.etNewName?.text.toString(),
                        profilePictureUri!!,
                        databaseReference
                    )
                } else {
                    updateName(binding?.etNewName?.text.toString(), databaseReference)
                }
            }
        }
    }

    private fun updateNameAndImage(
        newName: String,
        profilePictureUri: Uri,
        databaseReference: DatabaseReference?
    ) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val userUid = currUser?.uid
        val avatarRef = storageRef.child("avatars/$userUid.jpg")

        avatarRef.putFile(profilePictureUri)
            .addOnSuccessListener {
                avatarRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val url = downloadUri.toString()

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(url))
                            .build()

                        currUser?.updateProfile(profileUpdates)

                        val hashMap = readUserInfo(databaseReference)
                        newName.let {
                            if (it.isNotEmpty()) hashMap["userName"] = it
                        }
                        hashMap["profileImage"] = url

                        this.databaseReference?.updateChildren(hashMap as Map<String, Any>)
                        makeToast("Success!")
                        findNavController().navigate(R.id.nav_from_settings_to_container)
                        binding?.applyChangesButton!!.isEnabled = true
                    }
            }.addOnFailureListener{
                makeToast("Something went wrong...")
                findNavController().navigate(R.id.nav_from_settings_to_container)
            }.addOnFailureListener{
                makeToast("Failed to update...")
                findNavController().navigate(R.id.nav_from_settings_to_container)
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            profilePictureUri = data.data
            val context = requireContext().applicationContext
            Glide.with(context)
                .load(profilePictureUri)
                .transform(CenterCrop())
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(binding!!.ivProfilePicture)
        }
    }

    private fun updateName(
        newName: String, databaseReference: DatabaseReference?
    ) {
        val hashMap = readUserInfo(databaseReference)

        if (newName.isEmpty()) {
            makeToast("Username must be non-empty.")
        } else if (newName.length > 20) {
            makeToast("Username is too long. Try shorter.")
        } else {
            hashMap["userName"] = newName
            databaseReference?.updateChildren(hashMap as Map<String, Any>)
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
