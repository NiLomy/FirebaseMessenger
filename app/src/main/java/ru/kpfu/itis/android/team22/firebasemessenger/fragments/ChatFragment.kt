package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.MessageAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentChatBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.Message
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.MessagesFirebaseService
import ru.kpfu.itis.android.team22.firebasemessenger.utils.IconUploader
import ru.kpfu.itis.android.team22.firebasemessenger.utils.NotificationSender
import java.time.LocalDateTime

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private var binding: FragmentChatBinding? = null
    private var currentUser: FirebaseUser? = null
    private var userID: String? = null
    private var adapter: MessageAdapter? = null
    private var mMessageList = ArrayList<Message>()

    private var rvPos : Int? = null
    private var preferences : SharedPreferences? = null
    private val APP_POSITIONS = "positions"
    private val PREF_CHAT_POS = "chatPos"

    private var justEntered = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = activity?.getSharedPreferences(APP_POSITIONS, Context.MODE_PRIVATE)
        rvPos = preferences?.getInt(PREF_CHAT_POS, 0)
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = binding?.rvMessages?.layoutManager as LinearLayoutManager
        val pos = layoutManager.findFirstCompletelyVisibleItemPosition()
        preferences?.edit()
            ?.putInt(PREF_CHAT_POS, pos)
            ?.apply()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        currentUser = FirebaseAuth.getInstance().currentUser
        rvPos?.let{binding?.rvMessages?.layoutManager?.scrollToPosition(it)}

        setStatusBarColor()
        initFirebaseToken()
        setUserInfo()
        setOnClickListeners()
        currentUser?.uid?.let { currentUserId ->
            userID?.let { userId -> updateChat(currentUserId, userId) }
        }
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
        val reference = userID?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }

        reference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val context = requireContext()
                val user = snapshot.getValue(User::class.java)
                binding?.tvUserName?.text = user?.userName
                binding?.ivProfileImage?.let { IconUploader.loadDrawableImage(context, user, it) }
            }
        })
    }

    private fun setOnClickListeners() {
        binding?.run {
            btnSendMsg.setOnClickListener {
                val message: String = etSendMsg.text.toString()

                if (message.isEmpty()) {
                    Snackbar.make(root, "Message is empty!", Snackbar.LENGTH_SHORT).show()
                    etSendMsg.setText("")
                } else {
                    currentUser?.uid?.let { currentUserId ->
                        userID?.let { userId ->
                            sendMessage(
                                currentUserId,
                                userId,
                                message,
                                LocalDateTime.now().toString()
                            )
                        }
                    }
                    etSendMsg.setText("")
                    val title = getString(R.string.messages)
                    val msg = ": $message"
                    userID?.let { userId ->
                        NotificationSender.generateMessageNotification(
                            currentUser,
                            userId,
                            title,
                            msg
                        )
                    }
                }
            }

            backButton.setOnClickListener {
                findNavController().navigate(R.id.nav_from_chat_to_container)
            }

            ivProfileImage.setOnClickListener {
                val bundle: Bundle = bundleOf(getString(R.string.user_id_tag) to userID, "from" to "chat")
                findNavController().navigate(R.id.nav_from_chat_to_user_profile, bundle)
            }
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
        updateChatsLists(senderId, receiverId)
    }

    private fun updateChatsLists(senderId: String, receiverId: String) {
        val currentUserReference: DatabaseReference = getDatabaseReference(senderId)
        val anotherUserReference: DatabaseReference = getDatabaseReference(receiverId)
        setUpChatsList(currentUserReference, receiverId)
        setUpChatsList(anotherUserReference, senderId)
    }

    private fun getDatabaseReference(userIdentifier: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users").child(
            userIdentifier
        )
    }

    private fun setUpChatsList(databaseReference: DatabaseReference, addId: String) {
        val chatsList: ArrayList<String> = ArrayList()
        databaseReference.child("chatsList")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatsList.clear()
                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val id = dataSnapShot.getValue(String::class.java)
                        if (id != null) {
                            chatsList.add(id)
                        }
                    }
                    if (!chatsList.contains(addId)) {
                        chatsList.add(addId)
                        databaseReference.child("chatsList").setValue(chatsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateChat(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Messages")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pos = mMessageList.size
                mMessageList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val message = dataSnapShot.getValue(Message::class.java)

                    if (message?.senderID == senderId && message.receiverID == receiverId ||
                        message?.senderID == receiverId && message.receiverID == senderId
                    ) {
                        mMessageList.add(message)
                    }
                }
                initAdapter()
                if (justEntered) {
                    rvPos?. let {binding?.rvMessages?.layoutManager?.scrollToPosition(it)}
                    justEntered = false
                } else {
                    binding?.rvMessages?.layoutManager?.scrollToPosition(pos)
                }
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

        binding?.rvMessages?.adapter = adapter
        adapter?.itemCount?.minus(1)?.let { binding?.rvMessages?.scrollToPosition(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        adapter = null
    }
}
