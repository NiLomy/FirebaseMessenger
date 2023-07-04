package ru.kpfu.itis.android.team22.firebasemessenger

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var _auth: FirebaseAuth? = null
    private val auth get() = _auth!!
    private var context : Context? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)
        _auth = Firebase.auth
        context = requireContext().applicationContext

        setUp()
    }

    private fun setUp() {
        val btnSignUp = binding.btnSignUp
        val etName = binding.etName
        val etEmail = binding.etEmail
        val etPassword = binding.etPassword
        val etConfirmPassword = binding.etConfirmPassword
        val btnLogin = binding.btnLogin

        btnSignUp.setOnClickListener {
            onSignUpClicked(etName.text.toString(), etEmail.text.toString(), etPassword.text.toString(), etConfirmPassword.text.toString())
        }

        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.nav_from_signup_to_login)
        }
    }

    private fun onSignUpClicked(userName: String, email: String, password: String, confirmPassword: String) {
        if (userName.isEmpty()) {
            showToast(getString(R.string.user_name_must_be_non_empty))
            return
        }
        if (email.isEmpty()) {
            showToast(getString(R.string.email_must_be_non_empty))
            return
        }
        if (password.isEmpty()) {
            showToast(getString(R.string.password_must_be_non_empty))
            return
        }
        if (password.length < 6) {
            showToast(getString(R.string.password_length_is_to_short))
            return
        }
        if (confirmPassword.isEmpty()) {
            showToast(getString(R.string.confirm_password))
            return
        }
        if (password != confirmPassword) {
            showToast(getString(R.string.passwords_dont_match))
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
                        showToast(getString(R.string.check_email))
                        binding.etEmail.error = ""
                    }
                }
        }
    }

    private fun saveUserDataToDatabase(databaseReference: DatabaseReference, hashMap: HashMap<String, String>) {
        databaseReference.setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    clearFields()
                    showToast(getString(R.string.registration_success))
                    // TODO: написать логику перехода на экран пользователя
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        binding.etName.setText("")
        binding.etEmail.setText("")
        binding.etPassword.setText("")
        binding.etConfirmPassword.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}