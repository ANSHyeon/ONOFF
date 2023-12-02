package com.anshyeon.onoff.ui.chatRoom

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.delay
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
        viewModel.getChatRoomOfPlace(args.placeName)
        database = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_REALTIME_DB_URL)
        chatRoomRef = database.getReference("message")
        observeCurrentUser()
    }

    private fun setLayout() {
        binding.rvMessageList.adapter = adapter
        binding.rvMessageList.addOnLayoutChangeListener(onLayoutChangeListener)
        binding.toolbarChat.title = args.placeName
        binding.chatRoomId = args.chatRoomId
        binding.viewModel = viewModel
        setNavigationOnClickListener()
        setImeSendActionListener()
        observeChatRoomKey()
    }

    private val onLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(10)
                    binding.rvMessageList.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

    private fun observeChatRoomKey() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.chatRoomKey.collect {
                        if (it.isNotBlank()) {
                            viewModel.messageList.collect { messageList ->
                                adapter.submitList(messageList)
                                binding.rvMessageList.scrollToPosition(adapter.itemCount - 1)
                                receiveMessages()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
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
                    val existingMessages = adapter.currentList
                    val isMessageExists =
                        existingMessages.any { it.messageId == newMessage.messageId }

                    if (!isMessageExists) {
                        val list = mutableListOf<Message>()
                        list.addAll(existingMessages)
                        list.add(newMessage)
                        adapter.submitList(list.sortedBy { it.sendAt })
                        viewModel.insertMessage(newMessage)
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(100)
                            binding.rvMessageList.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatRoomRef.removeEventListener(messageListener)
    }
}