package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsDialogBinding

class SettingsDialogFragment : DialogFragment(R.layout.fragment_settings_dialog) {
    private var _binding: FragmentSettingsDialogBinding? = null
    private val binding get() = _binding!!

    private var user : FirebaseUser? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsDialogBinding.bind(view)
        user = FirebaseAuth.getInstance().currentUser
        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.run {
            applyChangesButton.setOnClickListener {
                var successful = true
                user?.let {
                    if (validatePasswords(
                            etNewPassword.text.toString(), etConfirmNewPassword.text.toString())) {
                        it.updatePassword(etNewPassword.text.toString()).addOnCompleteListener {task ->
                            if (!task.isSuccessful) successful = false
                        }
                    }
                    if (successful) showSnackbar("Successfully changed!")
                    else showSnackbar("Something went wrong...")
                }

                user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val newEmail = etNewEmail.text.toString()
                    if (newEmail.isNotEmpty()) {
                        it.updateEmail(etNewEmail.text.toString()).addOnCompleteListener { task ->
                            if (!task.isSuccessful) successful = false
                        }
                    }
                }
                if (successful) {
                    showSnackbar("Successfully changed!")
                    dismiss()
                }
                else showSnackbar("Something went wrong...")
            }
        }
    }

    private fun validatePasswords(password : String?, confirmPassword : String?) : Boolean{
        if (password == null || confirmPassword == null) return false
        else if (password.isEmpty() && confirmPassword.isEmpty()) return false
        else if (password.isEmpty()) {
            showSnackbar(getString(R.string.password_must_be_non_empty))
            return false
        }
        else if (password.length < 6) {
            showSnackbar(getString(R.string.password_length_is_to_short))
            return false
        }
        else if (confirmPassword.isEmpty()) {
            showSnackbar(getString(R.string.confirm_password))
            return false
        }
        else if (password != confirmPassword) {
            showSnackbar(getString(R.string.passwords_dont_match))
            return false
        }
        else return true

    }

    private fun showSnackbar(message: String) {
        val rootView = view ?: return // Проверка, что view не равно null
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }
}