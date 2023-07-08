package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.NotificationAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentProfileBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var auth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null
    private var context: Context? = null
    private var adapter: NotificationAdapter? = null
    private val notificationsList: ArrayList<User> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileBinding.bind(view)
        context = requireContext()
        auth = Firebase.auth
        firebaseUser = auth?.currentUser

        initFields()
        setUpButtons()
    }

    private fun initFields() {
        databaseReference = firebaseUser?.uid?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
        }

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                binding.run {
                    userName.text = user?.userName

                    if (isAdded) {
                        val context = requireContext().applicationContext
                        Glide.with(context)
                            .load(user?.profileImage)
                            .placeholder(R.drawable.loading)
                            .error(R.drawable.error)
                            .into(ivImage)
                    }
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

        binding.ibNotifications.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog)
            val btn = dialog.findViewById<View>(R.id.cancel_btn)
            val clr = dialog.findViewById<View>(R.id.clear_all_btn)
            val rv = dialog.findViewById<RecyclerView>(R.id.rv_notifications)
            setUpNotifications(rv, dialog)
            btn.setOnClickListener {
                dialog.dismiss()
            }
            clr.setOnClickListener {
                val list2: ArrayList<String> = ArrayList()
                val firebase: FirebaseUser? = Firebase.auth.currentUser
                firebase?.uid?.let { it1 ->
                    FirebaseDatabase.getInstance().getReference("Users").child(it1)
                        .child("notificationsList")
                        .setValue(list2)
                }
            }
            dialog.show()
        }
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
        val firebase: FirebaseUser? = Firebase.auth.currentUser
        val currentUserDatabaseReference: DatabaseReference? =
            firebase?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
                    .child("notificationsList")
            }
        val list: ArrayList<String> = ArrayList()
        currentUserDatabaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val id = dataSnapShot.getValue(String::class.java)
                    id?.let { list.add(it) }
                }
                if (list.isEmpty()) {
                    binding.ibNotifications.setImageResource(R.drawable.ic_notifications)
                } else {
                    binding.ibNotifications.setImageResource(R.drawable.ic_has_notifications)
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

        adapter = NotificationAdapter(
            list = notificationsList,
            glide = Glide.with(this),
            controller = findNavController(),
            userId = getString(R.string.user_id_tag),
            dialog = dialog,
            currentUser = Firebase.auth.currentUser
        )
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(requireContext())
    }
}
