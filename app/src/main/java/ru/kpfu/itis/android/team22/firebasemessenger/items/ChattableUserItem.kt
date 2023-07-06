package ru.kpfu.itis.android.team22.firebasemessenger.items

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemFriendToChatBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class ChattableUserItem(
    private val binding: ItemFriendToChatBinding,
    private val glide: RequestManager,
    private val onItemClick: (User) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    fun onBind(user: User) {
        binding.run {
            userName.text = user.userName

            glide
                .load(user.profileImage)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivImage)

            root.setOnClickListener {
                onItemClick(user)
            }
        }
    }
}