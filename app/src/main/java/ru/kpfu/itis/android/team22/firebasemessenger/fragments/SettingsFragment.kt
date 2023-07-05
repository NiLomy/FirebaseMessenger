package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val galleryButton = binding.galleryButton
    private val changesButton = binding.applyChangesButton
    private val etNewName = binding.etNewName
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)
        var newImageUri : Uri? = null
        galleryButton.setOnClickListener {
            newImageUri = setProfilePicture()
        }
        /*changesButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            user?.run {
                val updateBuilder = UserProfileChangeRequest.Builder()
                newImageUri?.run { updateBuilder.setPhotoUri(this) }
                etNewName.text?.run { updateBuilder.setDisplayName(this.toString()) }
                val update = updateBuilder.build()
                user.updateProfile(update)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Profile was successfully updated!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(context, "Not able to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                Toast.makeText(context, etNewName.text.toString(), Toast.LENGTH_SHORT).show()
            }
        }*/
    }

    private fun setProfilePicture() : Uri?{
        //TODO: заставить эту штуку работать
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        val imageUri : Uri? = null
        return imageUri
        //с кодом сверху хотя бы не вылетает

        /*val profilePicture = binding.ivProfilePicture
        var imageUri : Uri? = null
        val changeImage =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                Toast.makeText(context, "NE NADO TAK GOVORIT", Toast.LENGTH_SHORT).show()
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    imageUri = data?.data
                    profilePicture.setImageURI(imgUri)
                }                }
        changeImage.launch(intent)*/
    }

}
