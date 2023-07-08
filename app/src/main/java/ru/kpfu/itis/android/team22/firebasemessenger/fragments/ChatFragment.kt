package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.MessageAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentChatBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.Message
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.MessagesFirebaseService
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.NotificationData
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.PushNotification
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.RetrofitInstance
import java.time.LocalDateTime

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private var binding: FragmentChatBinding? = null
    // Если раскомментировать, то иногда будет ошибка NullPointerException
//    private val binding get() = _binding!!
    private var currentUser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var userID: String? = null
    private var mMessageList = ArrayList<Message>()
    private var adapter: MessageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChatBinding.bind(view)


        setStatusBarColor()
        initFirebaseToken()
        setUserInfo()
        setOnClickListeners()
        updateChat(currentUser!!.uid, userID!!)
    }

    private fun setStatusBarColor() {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window?.statusBarColor = resources.getColor(R.color.colorPrimary)
    }

    private fun initFirebaseToken() {
        MessagesFirebaseService.sharedPref =
            activity?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        MessagesFirebaseService.token = task.result!!
                    }
                }
            }
    }

    private fun setUserInfo() {
        userID = arguments?.getString("id")

        currentUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID!!)

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding?.tvUserName?.text = user!!.userName
                val context = requireContext().applicationContext
                Glide.with(context)
                    .load(user.profileImage)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(binding?.ivProfileImage!!)

            }
        })
    }

    private fun setOnClickListeners() {
        binding!!.btnSendMsg.setOnClickListener {
            val message: String = binding!!.etSendMsg.text.toString()

            if (message.isEmpty()) {
                Snackbar.make(binding!!.root, "Message is empty!", Snackbar.LENGTH_SHORT).show()
                binding!!.etSendMsg.setText("")
            } else {
                sendMessage(currentUser!!.uid, userID!!, message, LocalDateTime.now().toString())
                binding!!.etSendMsg.setText("")
                PushNotification(
                    NotificationData(getString(R.string.messages), currentUser!!.displayName!! + ": " + message),
                    "/topics/msg_$userID"
                )
                    .also {
                        sendNotification(it)
                    }
            }
        }

        binding!!.backButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_chat_to_container)
        }

        binding!!.ivProfileImage.setOnClickListener {
            val bundle: Bundle = bundleOf(getString(R.string.user_id_tag) to userID)
            findNavController().navigate(R.id.nav_from_chat_to_user_profile, bundle)
        }
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String, time: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["senderID"] = senderId
        hashMap["receiverID"] = receiverId
        hashMap["message"] = message
        hashMap["time"] = time

        reference.child("Messages").push().setValue(hashMap)
    }

    private fun updateChat(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Messages")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMessageList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val message = dataSnapShot.getValue(Message::class.java)

                    if (message!!.senderID == senderId && message.receiverID == receiverId ||
                        message.senderID == receiverId && message.receiverID == senderId
                    ) {
                        mMessageList.add(message)
                    }
                }
                initAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initAdapter() {
        if (!isAdded || isDetached || activity == null) {
            // The fragment is not yet linked to the activity
            return
        }

        adapter = MessageAdapter(
            context = requireContext(),
            list = mMessageList
        )

        binding!!.rvMessages.adapter = adapter
        binding!!.rvMessages.scrollToPosition(adapter!!.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        adapter = null
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d("PUSH", "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e("PUSH", response.errorBody()!!.string())
                }
            } catch (e: Exception) {
                Log.e("PUSH", e.toString())
            }
        }
}
