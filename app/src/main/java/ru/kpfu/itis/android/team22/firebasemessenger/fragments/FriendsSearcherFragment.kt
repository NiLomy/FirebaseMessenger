package ru.kpfu.itis.android.team22.firebasemessenger.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kpfu.itis.android.team22.firebasemessenger.R
import ru.kpfu.itis.android.team22.firebasemessenger.databinding.FragmentFriendsSearcherBinding

class FriendsSearcherFragment : Fragment(R.layout.fragment_friends_searcher) {
    private var _binding: FragmentFriendsSearcherBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendsSearcherBinding.bind(view)
        setUpButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpButtons() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.nav_from_friends_searcher_to_friends_list)
        }
    }
}
