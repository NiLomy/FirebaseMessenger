package ru.kpfu.itis.android.team22.firebasemessenger.items

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.ItemUserBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class UserItem(
    private val binding: ItemUserBinding,
    private val glide: RequestManager,
    private val onItemClick: (User) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private val options: RequestOptions = RequestOptions
        .diskCacheStrategyOf(DiskCacheStrategy.ALL)

    fun onBind(user: User) {
        binding.run {
            userName.text = user.userName

            val storageRef = FirebaseStorage.getInstance().reference.child(user.profileImage)
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                glide
                    .load(uri)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .apply(options)
                    .into(ivImage)
            }.addOnFailureListener {
                Toast.makeText(root.context, it.message, Toast.LENGTH_SHORT).show()
            }

            root.setOnClickListener {
                onItemClick(user)
            }
        }
    }
}
