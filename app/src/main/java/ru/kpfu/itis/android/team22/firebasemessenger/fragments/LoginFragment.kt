package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentLoginBinding


class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var _auth: FirebaseAuth? = null
    private val auth get() = _auth!!
    private var firebaseUser: FirebaseUser? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)
        _auth = Firebase.auth

        firebaseUser = auth.currentUser

        if (firebaseUser != null) {
            findNavController().navigate(R.id.nav_from_login_to_container)
        }

        setUp()
    }

    private fun setUp() {
        val etEmail = binding.etEmail
        val etPassword = binding.etPassword
        val btnLogin = binding.btnLogin
        val btnSignUp = binding.btnSignUp

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (validateEmailAndPassword(email, password)) {
                signInWithEmailAndPassword(email, password)
            } else {
                showSnackbar(getString(R.string.something_went_wrong))
            }
        }

        btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.nav_from_login_to_signup)
        }
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    showSnackbar(getString(R.string.login_success))
                    findNavController().navigate(R.id.nav_from_login_to_container)
                } else {
                    showSnackbar(getString(R.string.invalid_credentials))
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