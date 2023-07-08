package ru.kpfu.itis.android.team22.firebasemessenger.adapters

import android.content.Context
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.entities.Message
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageAdapter(private val context: Context, private val list: ArrayList<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMsg: TextView = view.findViewById(R.id.tv_msg)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val ivUserImage: CircleImageView = view.findViewById(R.id.iv_user_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == RECIEVED_MSG) {
            createReceivedViewHolder(parent)
        } else {
            createSentViewHolder(parent)
        }
    }

    private fun createReceivedViewHolder(parent: ViewGroup): ViewHolder {
        val view = from(parent.context).inflate(R.layout.item_sent_msg, parent, false)
        return ViewHolder(view)
    }

    private fun createSentViewHolder(parent: ViewGroup): ViewHolder {
        val view = from(parent.context).inflate(R.layout.item_recieved_msg, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = list[position]
        bindMessageDetails(holder, msg)
        loadUserProfileImage(holder, msg.senderID)
    }

    private fun bindMessageDetails(holder: ViewHolder, message: Message) {
        holder.tvMsg.text = message.message
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        holder.tvTime.text = LocalDateTime.parse(message.time).format(formatter)
    }

    private fun loadUserProfileImage(holder: ViewHolder, senderID: String) {
        val currentUserUid = Firebase.auth.currentUser?.uid
        val authorUid = senderID.takeIf { it == currentUserUid } ?: senderID

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")
        val authorRef = databaseReference.child(authorUid)

        authorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                user?.let { loadImage(holder, it.profileImage) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadImage(holder: ViewHolder, profileImage: String?) {
        Glide
            .with(context)
            .load(profileImage)
            .transform(CenterCrop())
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .apply(options)
            .into(holder.ivUserImage)

    }

    override fun getItemViewType(position: Int): Int {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (list[position].senderID == firebaseUser?.uid) {
            RECIEVED_MSG
        } else {
            SEND_MSG
        }
    }

    companion object {
        const val SEND_MSG = 0
        const val RECIEVED_MSG = 1
    }
}