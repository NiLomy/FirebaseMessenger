package ru.kpfu.itis.android.team22.firebasemessenger.entities

data class Chat(
    var senderID: String = "",
    var receiverID: String = "",
    var message: String = ""
)