package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserToAddBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class AddableUserItem(
    private val binding: ItemUserToAddBinding,
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
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivImage)

            ib.setOnClickListener {

            }

            root.setOnClickListener {
                val bundle : Bundle = bundleOf(userId to user.userId)
                controller.navigate(R.id.nav_from_friends_searcher_to_user_profile, bundle)
            }
        }
    }
}
