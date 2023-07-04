package ru.kpfu.itis.android.team22.firebasemessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val auth = Firebase.auth

        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)
        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)


        btnSignUp.setOnClickListener {
            val userName = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (userName.isEmpty()) {
                Toast.makeText(applicationContext, "Username must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(applicationContext, "Email must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(applicationContext, "Password must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6)  {
                Toast.makeText(applicationContext, "Password must be longer than 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Confirm your password!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(applicationContext, "Passwords don't match...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: если пароль слабый, то без объявления ошибки не регает пользователя - починить
            // TODO: как-то уведомлять если пользователь с такой почтой уже есть
            // Initial task failed for action RecaptchaAction(action=signUpPassword)with exception - The given password is invalid

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        val userId: String = user!!.uid

                        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                        val hashMap: HashMap<String, String> = HashMap()
                        hashMap.put("userId", userId)
                        hashMap.put("userName", userName)
                        hashMap.put("profileImage", "")
                        Toast.makeText(this, "aaa!", Toast.LENGTH_SHORT).show()

                        databaseReference.setValue(hashMap).addOnCompleteListener(this) {
                            if (it.isSuccessful) {
                                etName.setText("")
                                etEmail.setText("")
                                etPassword.setText("")
                                etConfirmPassword.setText("")

                                // TODO: написать логику перехода на экран пользователя
                                Toast.makeText(this, "OK!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

        }

        // TODO: переписать на переход на фрагмент логина
        btnLogin.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
        }
    }
}