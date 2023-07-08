package ru.kpfu.itis.android.team22.firebasemessenger.entities

data class Message(
    var senderID: String = "",
    var receiverID: String = "",
    var message: String = "",
    var time: String = ""
)
