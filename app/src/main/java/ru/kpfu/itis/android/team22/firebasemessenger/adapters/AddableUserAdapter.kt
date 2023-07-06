package ru.kpfu.itis.android.team22.firebasemessenger.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import de.hdodenhof.circleimageview.CircleImageView
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserToAddBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User
import ru.kpfu.itis.android.team22.firebasemessenger.items.AddableUserItem

class AddableUserAdapter(
    private var list: ArrayList<User>,
    private val glide: RequestManager,
    private val onItemClick: (User) -> Unit,
    private val context: Context,
) : RecyclerView.Adapter<AddableUserItem>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddableUserItem = AddableUserItem(
        binding = ItemUserToAddBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
        glide = glide,
        onItemClick = onItemClick,
    )

    override fun onBindViewHolder(holder: AddableUserItem, position: Int) {
        holder.onBind(list[position])
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun filter(newList : ArrayList<User>) {
        list = newList
        notifyDataSetChanged()
    }
}
