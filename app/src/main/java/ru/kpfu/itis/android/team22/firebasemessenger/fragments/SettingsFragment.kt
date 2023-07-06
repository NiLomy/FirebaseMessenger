package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        with(binding) {
            var newImageUri: Uri? = null
            val user = FirebaseAuth.getInstance().currentUser
            databaseReference =
                user?.uid?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }

            galleryButton.setOnClickListener {
                newImageUri = getProfilePicture()
            }
            fabToContainer.setOnClickListener {
                findNavController().navigate(R.id.nav_from_settings_to_container)
            }
            applyChangesButton.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.run {
                    updateName(etNewName.text.toString(), databaseReference, currentUser)
                }
            }
        }
    }

    private fun getProfilePicture(): Uri? {
        //TODO: заставить эту штуку работать
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        var imageUri: Uri? = null
        val profilePicture = _binding?.ivProfilePicture
        val changeImage =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    imageUri = data?.data
                    profilePicture?.setImageURI(imageUri)
                }
            }
        changeImage.launch(intent)
        return imageUri
    }

    private fun updateName(
        newName: String, databaseReference: DatabaseReference?,
        user: FirebaseUser
    ) {
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["userName"] = newName
        hashMap["userId"] = user.uid
        hashMap["profileImage"] = user.photoUrl.toString()

        databaseReference?.setValue(hashMap)
    }
}
