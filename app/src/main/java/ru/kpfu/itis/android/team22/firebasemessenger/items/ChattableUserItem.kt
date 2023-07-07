package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemFriendToChatBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class ChattableUserItem(
    private val binding: ItemFriendToChatBinding,
    private val glide: RequestManager,
    private val onItemClick: (User) -> Unit,
    private val controller: NavController,
    private val userId: String,
) : RecyclerView.ViewHolder(binding.root) {

    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    fun onBind(user: User) {
        binding.run {
            userName.text = user.userName

            glide
                .load(user.profileImage)
                .transform(CenterCrop())
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivImage)

            ib.setOnClickListener {
                val bundle: Bundle = bundleOf("id" to user.userId)
                controller.navigate(R.id.nav_from_friends_list_to_chat, bundle)
            }

            root.setOnClickListener {
                val bundle : Bundle = bundleOf(userId to user.userId, "from" to "friends")
                controller.navigate(R.id.nav_from_friends_list_to_user_profile, bundle)
            }
        }
    }
}