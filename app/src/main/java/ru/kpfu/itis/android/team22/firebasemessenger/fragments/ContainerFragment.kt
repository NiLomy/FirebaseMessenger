package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentContainerBinding

class ContainerFragment : Fragment(R.layout.fragment_container) {
    private var _binding: FragmentContainerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentContainerBinding.bind(view)

        val bottomNavView = binding.bnv

        handleNavigation(MessagesFragment())

        bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_messages -> {
                    handleNavigation(MessagesFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.item_profile -> {
                    handleNavigation(ProfileFragment())
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        pushNotificationTest()
    }

    private fun pushNotificationTest() {

    }

    private fun handleNavigation(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.bnv_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}