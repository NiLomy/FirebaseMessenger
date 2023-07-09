package ru.kpfu.itis.android.team22.firebasemessenger.utils

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.NotificationData
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.PushNotification
import ru.kpfu.itis.android.team22.firebasemessenger.notifications.RetrofitInstance

class NotificationSender {
    companion object {
        private const val FRIEND_ADDING_TITLE = "You have a new friend!"
        private const val FRIEND_REMOVING_TITLE = "Bad news..."
        private const val FRIEND_ADDING_MESSAGE = " just added you to his friends."
        private const val FRIEND_REMOVING_MESSAGE = " just removed you from his friends."
        private const val FRIEND_TOPIC = "/topics/friend_"
        private const val MESSAGE_TOPIC = "/topics/msg_"

        fun generateFriendAddingNotification(currentUser: FirebaseUser?, userIdentifier: String) {
            generateNotification(
                currentUser,
                userIdentifier,
                FRIEND_ADDING_TITLE,
                FRIEND_ADDING_MESSAGE,
                FRIEND_TOPIC
            )
        }

        fun generateFriendRemovingNotification(currentUser: FirebaseUser?, userIdentifier: String) {
            generateNotification(
                currentUser,
                userIdentifier,
                FRIEND_REMOVING_TITLE,
                FRIEND_REMOVING_MESSAGE,
                FRIEND_TOPIC
            )
        }

        fun generateMessageNotification(
            currentUser: FirebaseUser?,
            userIdentifier: String,
            title: String,
            message: String
        ) {
            generateNotification(
                currentUser,
                userIdentifier,
                title,
                message,
                MESSAGE_TOPIC
            )
        }

        private fun generateNotification(
            currentUser: FirebaseUser?,
            userIdentifier: String,
            title: String,
            message: String,
            topic: String
        ) {
            PushNotification(
                NotificationData(
                    title,
                    currentUser?.displayName + message
                ),
                topic + userIdentifier
            )
                .also {
                    sendNotification(it)
                }
        }

        private fun sendNotification(notification: PushNotification) =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.postNotification(notification)
                    if (response.isSuccessful) {
                        Log.d("PUSH", "Response: ${Gson().toJson(response)}")
                    } else {
                        response.errorBody()?.string()?.let { Log.e("PUSH", it) }
                    }
                } catch (e: Exception) {
                    Log.e("PUSH", e.toString())
                }
            }
    }
}
