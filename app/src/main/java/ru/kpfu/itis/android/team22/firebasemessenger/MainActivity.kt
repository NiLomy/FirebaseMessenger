package ru.kpfu.itis.android.team22.firebasemessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    // TODO: возможно, есть способ получше пофиксить этот баг
    // Blocking transition to the previous fragment
    // Necessary to avoid exceptional situations
//    override fun onBackPressed() {
//    }
}