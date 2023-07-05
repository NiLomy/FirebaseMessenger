package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentContainerBinding

class ContainerFragment : Fragment(R.layout.fragment_container) {
    // TODO: сделать так, чтоб нельзя было возвращаться назад, если пользователь уже зареган
    // TODO: последний профиль в списке не видно до конца (RV налезает на BNV)

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