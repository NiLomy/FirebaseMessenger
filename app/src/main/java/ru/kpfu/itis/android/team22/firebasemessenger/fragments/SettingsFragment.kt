package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private var databaseReference: DatabaseReference? = null

    val etNewName = _binding?.etNewName
    //private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)
        val galleryButton = _binding?.galleryButton
        val changesButton = _binding?.applyChangesButton
        val etNewName = _binding?.etNewName
        val fabToProfile = _binding?.fabToContainer
        var newImageUri : Uri? = null
        val user = FirebaseAuth.getInstance().currentUser
        databaseReference =
            user?.uid?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }

        galleryButton?.setOnClickListener {
            newImageUri = getProfilePicture()
        }
        fabToProfile?.setOnClickListener {
            findNavController().navigate(R.id.nav_from_settings_to_container)
        }
        changesButton?.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.run {
                updateName(etNewName?.text.toString(), databaseReference, user)
            }
        }
    }

    private fun getProfilePicture() : Uri?{
        //TODO: заставить эту штуку работать
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        var imageUri : Uri? = null
        val profilePicture = _binding?.ivProfilePicture
        val changeImage =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    imageUri = data?.data
                    profilePicture?.setImageURI(imageUri)
                }                }
        changeImage.launch(intent)
        return imageUri
    }

    private fun updateName(newName : String?, databaseReference: DatabaseReference?,
                           user : FirebaseUser?) {
        val hashMap : HashMap<String, String> = HashMap()
        newName?.let {hashMap.put("userName", it)}
        user?.let {
            hashMap.put("userId", it.uid)
            newName?.let {name -> hashMap.put("userName", name)}
            //хз сработает ли
            hashMap.put("profileImage", it.photoUrl.toString())
        }

        databaseReference?.setValue(hashMap)

    }

}
