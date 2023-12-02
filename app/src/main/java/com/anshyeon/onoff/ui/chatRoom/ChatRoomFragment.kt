package com.anshyeon.onoff.ui.chatRoom

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.anshyeon.onoff.BuildConfig
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.databinding.FragmentChatRoomBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding>(R.layout.fragment_chat_room) {

    private lateinit var database: FirebaseDatabase
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var messageListener: ChildEventListener
    private val args: ChatRoomFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatRoomViewModel>()
    private val adapter = ChatRoomAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUser()
        viewModel.getMessage(args.chatRoomId)

        database = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_REALTIME_DB_URL)
        chatRoomRef = database.getReference("message")
        observeCurrentUser()
    }

    private fun setLayout() {
        binding.rvMessageList.adapter = adapter
        binding.toolbarChat.title = args.placeName
        binding.chatRoomId = args.chatRoomId
        binding.viewModel = viewModel
        setNavigationOnClickListener()
        setImeSendActionListener()
        observeMessageListList()
        receiveMessages()
    }

    private fun observeMessageListList() {
        lifecycleScope.launch {
            viewModel.messageList.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                if (it.isNotEmpty()) {
                    adapter.submitList(it)
                    binding.rvMessageList.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
    }

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            viewModel.currentUser.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let {
                    adapter.setCurrentUserEmail(it.email)
                    setLayout()
                }
            }
        }
    }

    private fun setNavigationOnClickListener() {
        binding.toolbarChat.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setImeSendActionListener() {
        binding.etChatSendText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                viewModel.createMessage(args.chatRoomId)
                true
            } else {
                false
            }
        }
    }

    private fun receiveMessages() {
        messageListener = chatRoomRef.orderByChild("chatRoomId").equalTo(args.chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val newMessage = dataSnapshot.getValue(Message::class.java) ?: return
                    if (adapter.currentList.isNotEmpty()) {
                        val list = mutableListOf<Message>()
                        list.addAll(adapter.currentList)
                        list.add(newMessage)
                        adapter.submitList(list)
                        binding.rvMessageList.scrollToPosition(adapter.itemCount - 1)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}