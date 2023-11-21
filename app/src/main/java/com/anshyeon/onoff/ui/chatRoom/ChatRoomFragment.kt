package com.anshyeon.onoff.ui.chatRoom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentChatRoomBinding
import com.anshyeon.onoff.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding>(R.layout.fragment_chat_room) {

    private val viewModel by viewModels<ChatRoomViewModel>()
    private val adapter = ChatRoomAdapter(viewModel)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUser()
        setLayout()
    }

    private fun setLayout() {
        binding.rvMessageList.adapter = adapter
        observeChatRoomList()
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
}