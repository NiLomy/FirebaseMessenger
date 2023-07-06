package ru.kpfu.itis.android.team22.firebasemessenger.adapters

import android.content.Context
import android.view.LayoutInflater.*
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.entities.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageAdapter(private val context: Context, private val list: ArrayList<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val SEND_MSG = 0
    private val RECIEVED_MSG = 1
    private var firebaseUser: FirebaseUser? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvMsg: TextView = view.findViewById(R.id.tv_msg)
        val tvTime : TextView = view.findViewById(R.id.tv_time)
        val ivUserImage: CircleImageView = view.findViewById(R.id.iv_user_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == RECIEVED_MSG) {
            val view = from(parent.context).inflate(R.layout.item_sent_msg, parent, false)
            ViewHolder(view)
        } else {
            val view = from(parent.context).inflate(R.layout.item_recieved_msg, parent, false)
            ViewHolder(view)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = list[position]
        holder.tvMsg.text = msg.message
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        holder.tvTime.text = LocalDateTime.parse(msg.time).format(formatter)

        // TODO: поменять на аватарку собеседника
        //Glide.with(context).load(user.profileImage).placeholder(R.drawable.profile_image).into(holder.imgUser)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (list[position].senderID == firebaseUser!!.uid) {
            RECIEVED_MSG
        } else {
            SEND_MSG
        }

    }
}