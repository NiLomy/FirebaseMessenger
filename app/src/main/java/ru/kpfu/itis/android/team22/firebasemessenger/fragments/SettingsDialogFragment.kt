package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentSettingsDialogBinding
import ru.kpfu.itis.android.team22.firebasemessenger.entities.User

class SettingsDialogFragment : DialogFragment(R.layout.fragment_settings_dialog) {
    private var binding: FragmentSettingsDialogBinding? = null
    private var currentUser: FirebaseUser? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsDialogBinding.bind(view)
        currentUser = FirebaseAuth.getInstance().currentUser

        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding?.run {
            applyChangesButton.setOnClickListener {
                var successful = true
                currentUser?.let { currentUser ->
                    if (etNewName.text.isNotEmpty()) {
                        updateName(etNewName.text.toString())
                    }

                    if (etNewPassword.text.isNotEmpty() || etConfirmNewPassword.text.isNotEmpty()) {
                        if (validatePasswords(
                                etNewPassword.text.toString(), etConfirmNewPassword.text.toString()
                            )
                        ) {
                            currentUser.updatePassword(etNewPassword.text.toString())
                                .addOnCompleteListener { task ->
                                    if (!task.isSuccessful) successful = false
                                    else {
                                        val credential =
                                            EmailAuthProvider.getCredential(
                                                this@SettingsDialogFragment.currentUser?.email.toString(),
                                                etNewPassword.text.toString()
                                            )
                                        this@SettingsDialogFragment.currentUser?.reauthenticate(
                                            credential
                                        )
                                            ?.addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    when (checkAndChangeEmail(etNewEmail)) {
                                                        FAIL_EMAIL_CODE -> successful = false
                                                    }
                                                } else showToast("Impossible")
                                            }
                                    }
                                }
                        } else successful = false
                    } else when (checkAndChangeEmail(etNewEmail)) {
                        FAIL_EMAIL_CODE -> successful = false
                        NO_EMAIL_CODE -> {
                            showToast("Nothing changed...")
                            dismiss()
                        }

                        else -> {}
                    }
                }
                if (successful) {
                    showToast("Successfully changed!")
                    dismiss()
                } else showToast("Something went wrong...")
            }
        }
    }

    private fun updateName(
        newName: String
    ) {
        val databaseReference = currentUser?.uid?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
        }

        val hashMap = readUserInfo(databaseReference)

        if (newName.isEmpty()) {
            showToast("Username must be non-empty.")
        } else if (newName.length > 20) {
            showToast("Username is too long. Try shorter.")
        } else {
            hashMap["userName"] = newName
            databaseReference?.updateChildren(hashMap as Map<String, Any>)

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser?.updateProfile(profileUpdates)
        }
    }

    private fun readUserInfo(databaseReference: DatabaseReference?): HashMap<String, String> {
        val hashMap: HashMap<String, String> = HashMap()
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                user?.run {
                    hashMap["profileImage"] = this.profileImage
                    hashMap["userId"] = this.userId
                    hashMap["userName"] = this.userName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message)
            }
        })
        return hashMap
    }


    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        if (password.isEmpty()) {
            showToast(getString(R.string.password_must_be_non_empty))
            return false
        }
        if (password.length < 6) {
            showToast(getString(R.string.password_length_is_to_short))
            return false
        }
        if (confirmPassword.isEmpty()) {
            showToast(getString(R.string.confirm_password))
            return false
        }
        if (password != confirmPassword) {
            showToast(getString(R.string.passwords_dont_match))
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAndChangeEmail(et: EditText): Int {
        if (et.text.isEmpty()) return NO_EMAIL_CODE
        var fail = false
        currentUser?.let { currentUser ->
            if (et.text.isNotEmpty()) {
                try {
                    // TODO: проверять, является ли вообще почтой
                    currentUser.updateEmail(et.text.toString())
                        .addOnCompleteListener { task -> if (!task.isSuccessful) fail = true }
                } catch (e: FirebaseAuthUserCollisionException) {
                    showToast("This email is already used")
                    fail = true
                }
            }
        }
        return if (fail) FAIL_EMAIL_CODE
        else SUCCESS_EMAIL_CODE
    }

    companion object {
        private const val NO_EMAIL_CODE = 1
        private const val FAIL_EMAIL_CODE = -1
        private const val SUCCESS_EMAIL_CODE = 0
    }
}
