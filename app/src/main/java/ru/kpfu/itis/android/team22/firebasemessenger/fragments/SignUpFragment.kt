package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var auth: FirebaseAuth? = null
    private var context: Context? = null
    private var firebaseUser: FirebaseUser? = null

    private val DEFAULT_IMG_URL =
        "https://firebasestorage.googleapis.com/v0/b/fir-messenger-187aa.appspot.com/o/default.png?alt=media&token=cfe96231-9136-485e-932a-bcf2f1e7fdb9"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)
        auth = Firebase.auth
        context = requireContext().applicationContext

        firebaseUser = auth?.currentUser

        if (firebaseUser != null) {
            findNavController().navigate(R.id.nav_from_signup_to_container)
        }

        setUp()
    }

    private fun setUp() {
        with(binding) {
            btnSignUp.setOnClickListener {
                onSignUpClicked(
                    etName.text.toString(),
                    etEmail.text.toString(),
                    etPassword.text.toString(),
                    etConfirmPassword.text.toString()
                )
            }

            btnLogin.setOnClickListener {
                findNavController().navigate(R.id.nav_from_signup_to_login)
            }
        }
    }

    private fun onSignUpClicked(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
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
        activity?.let { fragmentActivity ->
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(fragmentActivity) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth?.currentUser

                        saveUserToFirebaseUser(user, email, userName)

                        val userId: String = user?.uid ?: ""

                        val databaseReference =
                            FirebaseDatabase.getInstance().getReference("Users").child(userId)

                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["userId"] = userId
                        hashMap["userName"] = userName
                        hashMap["profileImage"] = DEFAULT_IMG_URL
                        hashMap["friendsList"] = ArrayList<String>()
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

    private fun saveUserToFirebaseUser(user: FirebaseUser?, email: String, userName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .setPhotoUri(Uri.parse(DEFAULT_IMG_URL))
            .build()

        user!!.updateProfile(profileUpdates)
        user.updateEmail(email)
    }

    private fun saveUserDataToDatabase(
        databaseReference: DatabaseReference,
        hashMap: HashMap<String, Any>
    ) {
        databaseReference.setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSnackbar(getString(R.string.registration_success))
                    findNavController().navigate(R.id.nav_from_signup_to_container)
                }
            }
    }

    private fun showSnackbar(message: String) {
        val rootView = view ?: return // Проверка, что view не равно null
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
