package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var _auth: FirebaseAuth? = null
    private val auth get() = _auth!!
    private var context : Context? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)
        _auth = Firebase.auth
        context = requireContext().applicationContext

        firebaseUser = auth.currentUser

        if (firebaseUser != null) {
            findNavController().navigate(R.id.nav_from_signup_to_container)
        }

        setUp()
    }

    private fun setUp() {
        val btnSignUp = binding.btnSignUp
        val etName = binding.etName
        val etEmail = binding.etEmail
        val etPassword = binding.etPassword
        val etConfirmPassword = binding.etConfirmPassword
        val btnLogin = binding.btnLogin
        val button = binding.buttonnn

        btnSignUp.setOnClickListener {
            onSignUpClicked(etName.text.toString(), etEmail.text.toString(), etPassword.text.toString(), etConfirmPassword.text.toString())
        }

        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.nav_from_signup_to_login)
        }

        button.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_settingsFragment)
        }
    }

    private fun onSignUpClicked(userName: String, email: String, password: String, confirmPassword: String) {
        if (userName.isEmpty()) {
            showSnackbar(getString(R.string.user_name_must_be_non_empty))
            return
        }
        if (email.isEmpty()) {
            showSnackbar(getString(R.string.email_must_be_non_empty))
            return
        }
        if (password.isEmpty()) {
            showSnackbar(getString(R.string.password_must_be_non_empty))
            return
        }
        if (password.length < 6) {
            showSnackbar(getString(R.string.password_length_is_to_short))
            return
        }
        if (confirmPassword.isEmpty()) {
            showSnackbar(getString(R.string.confirm_password))
            return
        }
        if (password != confirmPassword) {
            showSnackbar(getString(R.string.passwords_dont_match))
            return
        }

        signUpUser(email, password, userName)
    }

    private fun signUpUser(email: String, password: String, userName: String) {
        // TODO: если пароль слабый, то без объявления ошибки не регает пользователя - починить
        // TODO: как-то уведомлять, если пользователь с такой почтой уже есть
        // Initial task failed for action RecaptchaAction(action=signUpPassword)with exception - The given password is invalid
        activity?.let { fragmentActivity ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(fragmentActivity) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        val userId: String = user!!.uid

                        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                        val hashMap: HashMap<String, String> = HashMap()
                        hashMap["userId"] = userId
                        hashMap["userName"] = userName
                        hashMap["profileImage"] = ""

                        saveUserDataToDatabase(databaseReference, hashMap)
                    } else {
                        val errorMessage: String? = task.exception?.message
                        if (errorMessage != null) {
                            showSnackbar(errorMessage)
                            binding.etEmail.error = errorMessage
                        }
                    }
                }
        }
    }

    private fun saveUserDataToDatabase(databaseReference: DatabaseReference, hashMap: HashMap<String, String>) {
        databaseReference.setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSnackbar(getString(R.string.registration_success))
                    findNavController().navigate(R.id.nav_from_signup_to_container)
                }
            }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}