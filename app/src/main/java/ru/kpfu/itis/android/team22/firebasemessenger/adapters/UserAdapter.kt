package ru.kpfu.itis.android.team22.firebasemessenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.items.UserItem

class UserAdapter(
    private var list: ArrayList<User>,
    private val glide: RequestManager,
    private val onItemClick: (User) -> Unit,
) : RecyclerView.Adapter<UserItem>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserItem = UserItem(
        binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
        glide = glide,
        onItemClick = onItemClick,
    )

    override fun onBindViewHolder(holder: UserItem, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun filter(newList : ArrayList<User>) {
        list = newList
        notifyDataSetChanged()
    }
}
