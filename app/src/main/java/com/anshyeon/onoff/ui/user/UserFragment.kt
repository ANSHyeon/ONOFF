package com.anshyeon.onoff.ui.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentUserBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.util.NetworkConnection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFragment : BaseFragment<FragmentUserBinding>(R.layout.fragment_user) {

    private val viewModel by viewModels<UserViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setNetworkErrorBar()
        setToolBar()
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            val visibility = if (it) {
                View.GONE
            } else {
                View.VISIBLE
            }
            binding.networkErrorBar.visibility = visibility
        }
    }

    private fun setToolBar() {
        binding.toolbarUser.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.user_app_bar_edit -> {
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
}