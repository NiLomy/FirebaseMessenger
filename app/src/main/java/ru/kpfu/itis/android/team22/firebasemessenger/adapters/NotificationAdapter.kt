package ru.kpfu.itis.android.team22.firebasemessenger.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseUser
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemProfileNotificationBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.items.NotificationItem

class NotificationAdapter(
    private var list: ArrayList<User>,
    private val glide: RequestManager,
    private val controller: NavController,
    private val userId: String,
    private val currentUser: FirebaseUser?,
    private val context: Context,
    private val dialog: Dialog,
) : RecyclerView.Adapter<NotificationItem>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationItem = NotificationItem(
        binding = ItemProfileNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
        glide = glide,
        controller = controller,
        userId = userId,
        currentUser = currentUser,
        context = context,
        dialog = dialog
    )

    override fun onBindViewHolder(holder: NotificationItem, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
