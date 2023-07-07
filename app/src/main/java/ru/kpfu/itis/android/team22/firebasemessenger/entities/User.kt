package ru.kpfu.itis.android.team22.firebasemessenger.entities

data class User(
    val userId: String = "",
    val userName: String = "",
    val profileImage: String = "",
    val friendsList: ArrayList<String> = ArrayList(),
)
