package com.anshyeon.onoff.ui.chatRoom

import android.annotation.SuppressLint
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
import com.anshyeon.onoff.util.NetworkConnection
import com.anshyeon.onoff.util.SamePlaceChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding>(R.layout.fragment_chat_room) {

    private lateinit var database: FirebaseDatabase
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var messageListener: ChildEventListener
    private val args: ChatRoomFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatRoomViewModel>()
    private lateinit var client: FusedLocationProviderClient
    private val adapter = ChatRoomAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_REALTIME_DB_URL)
        chatRoomRef = database.getReference("message")
        setLayout()
    }

    private fun setLayout() {
        setAdapter()
        setBinding()
        setNavigationOnClickListener()
        setImeSendActionListener()
        setNetworkErrorBar()
    }

    private fun setBinding() {
        binding.toolbarChat.title = args.placeName
        binding.chatRoomId = args.chatRoomId
        binding.viewModel = viewModel
    }

    private fun setAdapter() {
        adapter.setCurrentUserEmail(viewModel.getLocalUserEmail())
        binding.rvMessageList.adapter = adapter
        binding.rvMessageList.addOnLayoutChangeListener(onLayoutChangeListener)
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            if (it) {
                viewModel.getMessage(args.chatRoomId)
                viewModel.getChatRoomOfPlace(args.placeName)
                checkSamePlace(args.chatRoomAddress)
                observeChatRoomKey()
                binding.networkErrorBar.visibility = View.GONE
            } else {
                viewModel.getLocalMessage(args.chatRoomId)
                binding.etChatSendText.isEnabled = false
                binding.networkErrorBar.visibility = View.VISIBLE
                observeLocalMessages()
            }
        }
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

    private fun observeLocalMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.localMessageList.collect { messageList ->
                        adapter.submitList(messageList)
                        binding.rvMessageList.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun checkSamePlace(chatRoomAddress: String) {
        client.lastLocation.addOnSuccessListener { location ->
            viewModel.getCurrentPlaceInfo(
                location.latitude.toString(),
                location.longitude.toString()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPlaceInfo.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let { placeInfo ->
                    if (SamePlaceChecker.isSamePlace(placeInfo, chatRoomAddress)) {
                        binding.etChatSendText.isEnabled = true
                        binding.tvErrorSamePlace.visibility = View.GONE
                    } else {
                        binding.tvErrorSamePlace.visibility = View.VISIBLE
                    }
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
}