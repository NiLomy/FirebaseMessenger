package ru.kpfu.itis.android.team22.firebasemessenger.entities

import java.sql.Date
import java.util.Calendar

data class Message(
    var senderID: String = "",
    var receiverID: String = "",
    var message: String = "",
    var time : String = ""
)