package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.NotificationAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.utils.IconUploader

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var binding: FragmentProfileBinding? = null
    private var auth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null
    private var currentUser: FirebaseUser? = null
    private var context: Context? = null
    private val notificationsList: ArrayList<User> = ArrayList()
    private lateinit var profilePictureUri: Uri


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        context = requireContext()
        auth = Firebase.auth
        currentUser = auth?.currentUser

        initFields()
        setUpButtons()
    }

    private fun initFields() {
        databaseReference = currentUser?.uid?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
        }

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                binding?.run {
                    userName.text = user?.userName
                    if (isAdded) {
                        val context = requireContext()
                        IconUploader.loadDrawableImage(context, user, ivImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpButtons() {
        binding?.run {
            friendsButton.setOnClickListener {
                findNavController().navigate(R.id.nav_from_container_to_friends_list)
            }

            fabSettings.setOnClickListener {
                val frag = SettingsDialogFragment()
                frag.show(parentFragmentManager, "data_change")
            }

            btnLogOut.setOnClickListener {
                auth?.signOut()
                findNavController().navigate(R.id.nav_from_container_to_login)
            }

            ibNotifications.setOnClickListener {
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.notifications_dialog)
                val btn = dialog.findViewById<View>(R.id.cancel_btn)
                val clr = dialog.findViewById<View>(R.id.clear_all_btn)
                val rv = dialog.findViewById<RecyclerView>(R.id.rv_notifications)

                setUpNotifications(rv, dialog)
                btn.setOnClickListener {
                    dialog.dismiss()
                }
                clr.setOnClickListener {
                    val list2: ArrayList<String> = ArrayList()
                    currentUser?.uid?.let { currentUserId ->
                        FirebaseDatabase.getInstance().getReference("Users").child(currentUserId)
                            .child("notificationsList")
                            .setValue(list2)
                    }
                }
                dialog.show()
            }

            ibGallery.setOnClickListener {
                binding?.ibGallery?.isEnabled = false
                openGallery()
            }
        }
    }

    private fun updateProfilePicture() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val userUid = currentUser?.uid
        val avatarRef = storageRef.child("avatars/$userUid.jpg")

        avatarRef.putFile(profilePictureUri)
            .addOnSuccessListener {
                avatarRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val url = downloadUri.toString()
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(url))
                            .build()
                        val hashMap = readUserInfo(databaseReference)

                        currentUser?.updateProfile(profileUpdates)

                        hashMap["profileImage"] = url

                        this.databaseReference?.updateChildren(hashMap as Map<String, Any>)
                        makeToast("Success!")
                    }
            }.addOnFailureListener {
                makeToast("Something went wrong...")
            }.addOnFailureListener {
                makeToast("Failed to update...")
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val context = requireContext()
            profilePictureUri = data.data!!
            binding?.ivImage?.let {
                IconUploader.loadUriImage(context, profilePictureUri, it)
            }
            makeToast("Updating your profile picture. Please wait.")
            updateProfilePicture()
        }
        binding?.ibGallery?.isEnabled = true
    }

    private fun setUpNotifications(rv: RecyclerView, dialog: Dialog) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")
        val list = getNotificationsList()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationsList.clear()
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (list.contains(user?.userId)) {
                        if (user != null) {
                            notificationsList.add(user)
                        }
                    }
                }
                initAdapter(rv, dialog)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getNotificationsList(): ArrayList<String> {
        val currentUserDatabaseReference: DatabaseReference? =
            currentUser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
                    .child("notificationsList")
            }
        val list: ArrayList<String> = ArrayList()
        currentUserDatabaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val id = dataSnapShot.getValue(String::class.java)
                    if (id != null) {
                        list.add(id)
                    }
                }
                if (list.isEmpty()) {
                    binding?.ibNotifications?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications,0,0, 0)
                } else {
                    binding?.ibNotifications?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_has_notifications,0,0, 0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        return list
    }

    private fun initAdapter(rv: RecyclerView, dialog: Dialog) {
        if (!isAdded || isDetached || activity == null) {
            // The fragment is not yet linked to the activity
            return
        }

        val adapter = context?.let {
            NotificationAdapter(
                list = notificationsList,
                glide = Glide.with(this),
                controller = findNavController(),
                userId = getString(R.string.user_id_tag),
                currentUser = Firebase.auth.currentUser,
                context = it,
                dialog = dialog,
            )
        }
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun makeToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
