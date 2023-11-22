package com.anshyeon.onoff.ui.chatRoom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentChatRoomBinding
import com.anshyeon.onoff.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding>(R.layout.fragment_chat_room) {

    private val args: ChatRoomFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatRoomViewModel>()
    private val adapter = ChatRoomAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUser()
        setLayout()
    }

    private fun setLayout() {
        binding.rvMessageList.adapter = adapter
        setNavigationOnClickListener()
        observeChatRoomList()
        observeCurrentUserEmail()
    }

    private fun observeChatRoomList() {
        lifecycleScope.launch {
            viewModel.dummyMessageList.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                adapter.submitMessageList(it)
            }
        }
    }

    private fun observeCurrentUserEmail() {
        lifecycleScope.launch {
            viewModel.currentUserEmail.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let {
                    adapter.setCurrentUserEmail(it)
                    viewModel.getMessage(args.chatRoomId)
                }
            }
        }
    }

    private fun setNavigationOnClickListener() {
        binding.toolbarChat.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}