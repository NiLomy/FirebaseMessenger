package ru.kpfu.itis.android.team22.firebasemessenger

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSignUpBinding

class SignUpFragment: Fragment(R.layout.fragment_sign_up) {
    private var binding: FragmentSignUpBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)

        val context = requireContext().applicationContext
        val auth = Firebase.auth

        val btnSignUp = binding?.btnSignUp
        val etName = binding?.etName
        val etEmail = binding?.etEmail
        val etPassword = binding?.etPassword
        val etConfirmPassword = binding?.etConfirmPassword
        val btnLogin = binding?.btnLogin


        btnSignUp?.setOnClickListener {
            val userName = etName?.text.toString()
            val email = etEmail?.text.toString()
            val password = etPassword?.text.toString()
            val confirmPassword = etConfirmPassword?.text.toString()

            if (userName.isEmpty()) {
                Toast.makeText(context, "Username must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(context, "Email must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(context, "Password must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6)  {
                Toast.makeText(context, "Password must be longer than 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(
                    context,
                    "Confirm your password!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords don't match...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: если пароль слабый, то без объявления ошибки не регает пользователя - починить
            // TODO: как-то уведомлять если пользователь с такой почтой уже есть
            // Initial task failed for action RecaptchaAction(action=signUpPassword)with exception - The given password is invalid

            activity?.let { fragmentActivity ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(fragmentActivity) {
                        if (it.isSuccessful) {
                            val user: FirebaseUser? = auth.currentUser
                            val userId: String = user!!.uid

                            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                            val hashMap: HashMap<String, String> = HashMap()
                            hashMap.put("userId", userId)
                            hashMap.put("userName", userName)
                            hashMap.put("profileImage", "")
                            Toast.makeText(context, "aaa!", Toast.LENGTH_SHORT).show()

                            databaseReference.setValue(hashMap).addOnCompleteListener(fragmentActivity) {
                                if (it.isSuccessful) {
                                    etName?.setText("")
                                    etEmail?.setText("")
                                    etPassword?.setText("")
                                    etConfirmPassword?.setText("")

                                    // TODO: написать логику перехода на экран пользователя
                                    Toast.makeText(context, "OK!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
            }

        }

        // TODO: переписать на переход на фрагмент логина
        btnLogin?.setOnClickListener {
            Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}