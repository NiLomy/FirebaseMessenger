package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentFriendsListBinding

class FriendsListFragment : Fragment(R.layout.fragment_friends_list) {
    private var _binding: FragmentFriendsListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendsListBinding.bind(view)
        setUpButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpButtons() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_friends_list_to_container)
        }

        binding.buttonFindFriends.setOnClickListener {
            findNavController().navigate(R.id.nav_from_friends_list_to_friends_searcher)
        }
    }
}
