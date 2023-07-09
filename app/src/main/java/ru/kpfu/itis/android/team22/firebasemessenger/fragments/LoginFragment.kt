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
    private var binding: FragmentLoginBinding? = null
    private var auth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        auth = Firebase.auth
        currentUser = auth?.currentUser

        if (currentUser != null) {
            findNavController().navigate(R.id.nav_from_login_to_container)
        }
        setUp()
    }

    private fun setUp() {
        binding?.run {
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
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding?.tvAnchor?.let {
                        showSnackbarCustomPos(getString(R.string.login_success), it)
                    }
                    findNavController().navigate(R.id.nav_from_login_to_container)
                } else {
                    showSnackbar(getString(R.string.invalid_credentials))
                }
            }
    }


    private fun showSnackbar(message: String) {
        binding?.root?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun showSnackbarCustomPos(message: String, view: View) {
        val rootView = this.view ?: return // Checking that view is not null
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(view)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
