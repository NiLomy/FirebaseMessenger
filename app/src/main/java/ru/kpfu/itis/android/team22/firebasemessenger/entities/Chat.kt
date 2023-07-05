package ru.kpfu.itis.android.team22.firebasemessenger.entities

data class Chat(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = ""
)