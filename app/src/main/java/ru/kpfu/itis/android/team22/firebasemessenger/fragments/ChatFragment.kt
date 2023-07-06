package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
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
import ru.kpfu.itis.android.team22.firebasemessenger.adapters.MessageAdapter
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentChatBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.Message
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import java.time.LocalDateTime

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var firebaseUser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var userID: String? = null
    private var mMessageList = ArrayList<Message>()
    private var adapter: MessageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChatBinding.bind(view)

        setUserName()
        setOnClickListeners()
        updateChat(firebaseUser!!.uid, userID!!)
    }

    private fun setUserName() {
        userID = arguments?.getString("id")

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID!!)

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.tvUserName.text = user!!.userName
            }
        })
    }

    private fun setOnClickListeners() {
        binding.btnSendMsg.setOnClickListener {
            val message: String = binding.etSendMsg.text.toString()

            if (message.isEmpty()) {
                Snackbar.make(binding.root, "Message is empty!", Snackbar.LENGTH_SHORT).show()
                binding.etSendMsg.setText("")
            } else {
                sendMessage(firebaseUser!!.uid, userID!!, message, LocalDateTime.now().toString())
                binding.etSendMsg.setText("")
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_chat_to_container)
        }
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String, time : String) {
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
        adapter = MessageAdapter(
            context = requireContext(),
            list = mMessageList
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
