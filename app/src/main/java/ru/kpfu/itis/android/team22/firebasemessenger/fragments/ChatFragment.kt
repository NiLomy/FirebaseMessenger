package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.ChatAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentChatBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.Chat
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User


class ChatFragment : Fragment(R.layout.fragment_chat) {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var _firebaseUser: FirebaseUser? = null
    private val firebaseUser get() = _firebaseUser!!

    private var _reference: DatabaseReference? = null
    private val reference get() = _reference!!

    private var _tvUserName: TextView? = null
    private val tvUserName get() = _tvUserName!!

    private var _btnSendMessage: ImageButton? = null
    private val btnSendMessage get() = _btnSendMessage!!

    private var _btnReturn: ImageButton? = null
    private val btnReturn get() = _btnReturn!!

    private var _etMessage: EditText? = null
    private val etMessage get() = _etMessage!!

    private var _userID: String? = null
    private val userID get() = _userID!!

    private var chatList = ArrayList<Chat>()
    private var adapter: ChatAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setUserName()
        setOnClickListeners()
        updateChat(firebaseUser.uid, userID)
    }

    private fun initViews(view: View) {
        _binding = FragmentChatBinding.bind(view)

        _tvUserName = binding.tvUserName
        _btnSendMessage = binding.btnSendMsg
        _btnReturn = binding.backButton
        _etMessage = binding.etSendMsg
    }

    private fun setUserName() {
        _userID = arguments?.getString("id")

        _firebaseUser = FirebaseAuth.getInstance().currentUser
        _reference = FirebaseDatabase.getInstance().getReference("Users").child(userID)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                tvUserName.text = user!!.userName
            }
        })
    }

    private fun setOnClickListeners() {
        btnSendMessage.setOnClickListener {
            val message: String = etMessage.text.toString()

            if (message.isEmpty()) {
                Snackbar.make(binding.root, "Message is empty!", Snackbar.LENGTH_SHORT).show()
                etMessage.setText("")
            } else {
                sendMessage(firebaseUser.uid, userID, message)
                etMessage.setText("")
            }
        }

        btnReturn.setOnClickListener {
            findNavController().navigate(R.id.nav_from_chat_to_container)
        }
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message

        reference.child("Chat").push().setValue(hashMap)
    }

    private fun updateChat(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderID == senderId && chat.receiverID == receiverId ||
                        chat.senderID == receiverId && chat.receiverID == senderId
                    ) {
                        chatList.add(chat)
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
        adapter = ChatAdapter(
            context = requireContext(),
            list = chatList
        )

        binding.rvMessages.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        adapter = null
    }

    // TODO: разобраться с onBackPressed(),
    //  запретить переход на предыдущий фрагмент,
    //  но позволить возвращаться с текущей переписки на главный экран
}
