package ru.kpfu.itis.android.team22.firebasemessenger.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class IconUploader {
    companion object {
        private val options: RequestOptions = RequestOptions
            .diskCacheStrategyOf(DiskCacheStrategy.ALL)

        fun loadDrawableImage(context: Context, user: User?, ivProfilePicture: ImageView) {
            Glide.with(context)
                .load(user?.profileImage)
                .transform(CenterCrop())
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivProfilePicture)
        }

        fun loadUriImage(context: Context, uri: Uri?, ivProfilePicture: ImageView) {
            Glide.with(context)
                .load(uri)
                .transform(CenterCrop())
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .apply(options)
                .into(ivProfilePicture)
        }
    }
}
