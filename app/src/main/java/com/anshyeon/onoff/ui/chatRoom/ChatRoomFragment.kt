package com.anshyeon.onoff.ui.chatRoom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.BuildConfig
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.databinding.FragmentChatRoomBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.setClickEvent
import com.anshyeon.onoff.ui.extensions.showMessage
import com.anshyeon.onoff.util.NetworkConnection
import com.anshyeon.onoff.util.SamePlaceChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding>(R.layout.fragment_chat_room) {

    private lateinit var database: FirebaseDatabase
    private lateinit var messageRef: DatabaseReference
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var messageListener: ChildEventListener
    private lateinit var memberListener: ChildEventListener
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
        messageRef = database.getReference("message")
        setLayout()
    }

    private fun setLayout() {
        setAdapter()
        setBinding()
        setNavigationOnClickListener()
        setNetworkErrorBar()
    }

    private fun setBinding() {
        binding.toolbarChat.title = args.chatRoom.placeName
        binding.viewModel = viewModel
    }

    private fun setAdapter() {
        adapter.setCurrentUserId(viewModel.getUserId())
        binding.rvMessageList.adapter = adapter
        binding.rvMessageList.addOnLayoutChangeListener(onLayoutChangeListener)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val insertedPosition = positionStart + itemCount - 1
                binding.rvMessageList.scrollToPosition(insertedPosition)
            }
        })
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            if (it) {
                handleNetworkConnection()
            } else {
                handleNetworkConnectionFailure()
            }
        }
    }

    private fun handleNetworkConnectionFailure() {
        viewModel.getLocalUser(args.chatRoom.memberList)
        observeLocalUserList()
        setImeSendActionFailureListener(R.string.error_message_retry)
        binding.ivChatSend.setClickEvent(viewLifecycleOwner.lifecycleScope) {
            binding.etChatSendText.showMessage(R.string.error_message_retry)
        }
        binding.networkErrorBar.visibility = View.VISIBLE
    }

    private fun handleNetworkConnection() {
        viewModel.getChatRoomOfPlace(args.chatRoom.placeName)
        observeChatRoomKey()
        binding.networkErrorBar.visibility = View.GONE
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
                            viewModel.getUserList(args.chatRoom.memberList)
                            checkSamePlace(
                                args.chatRoom.address,
                                args.chatRoom.latitude.toDouble(),
                                args.chatRoom.longitude.toDouble()
                            )
                        }
                    }
                }
                launch {
                    viewModel.isUserSaved.collect {
                        if (it) {
                            viewModel.getLocalUser(viewModel.memberIdList.toList())
                            observeLocalUserList()
                        }
                    }
                }
            }
        }
    }

    private fun observeLocalUserList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.localUserList.collect {
                        if (it.isNotEmpty()) {
                            adapter.setUserList(it)
                            receiveMessages()
                            receiveMembers()
                            viewModel.getLocalMessage(args.chatRoom.chatRoomId)
                            observeLocalMessages()
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
                        viewModel.setComplete()
                    }
                }
            }
        }
    }

    private fun checkSamePlace(
        chatRoomAddress: String,
        selectedLatitude: Double,
        selectedLongitude: Double
    ) {
        client.lastLocation.addOnSuccessListener { location ->
            val currentLatitude = location.latitude
            val currentLongitude = location.longitude
            viewModel.getCurrentPlaceInfo(currentLatitude.toString(), currentLongitude.toString())

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.currentPlaceInfo.flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED,
                ).collect {
                    it?.let { placeInfo ->
                        if (SamePlaceChecker.isSamePlace(
                                placeInfo,
                                chatRoomAddress,
                                currentLatitude,
                                currentLongitude,
                                selectedLatitude,
                                selectedLongitude
                            )
                        ) {
                            setImeSendActionListener()
                            binding.ivChatSend.setClickEvent(viewLifecycleOwner.lifecycleScope) {
                                viewModel.createMessage(args.chatRoom.chatRoomId)
                            }
                            binding.tvErrorSamePlace.visibility = View.GONE
                        } else {
                            setImeSendActionFailureListener(R.string.error_message_not_same_place)
                            binding.ivChatSend.setClickEvent(viewLifecycleOwner.lifecycleScope) {
                                binding.etChatSendText.showMessage(R.string.error_message_not_same_place)
                            }
                            binding.tvErrorSamePlace.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun setImeSendActionListener() {
        binding.etChatSendText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                viewModel.createMessage(args.chatRoom.chatRoomId)
                true
            } else {
                false
            }
        }
    }

    private fun setImeSendActionFailureListener(@StringRes messageId: Int) {
        binding.etChatSendText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                binding.etChatSendText.showMessage(messageId)
                true
            } else {
                false
            }
        }
    }

    private fun setNavigationOnClickListener() {
        binding.toolbarChat.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun receiveMessages() {
        messageListener = messageRef.orderByChild("chatRoomId").equalTo(args.chatRoom.chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val newMessage = dataSnapshot.getValue(Message::class.java) ?: return
                    viewModel.insertMessage(newMessage)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun receiveMembers() {
        var isFirstLoaded = false
        chatRoomRef = database.getReference("chatRoom/${viewModel.chatRoomKey.value}/memberList")
        memberListener = chatRoomRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                if (isFirstLoaded) {
                    val newUser = dataSnapshot.getValue(String::class.java) ?: return
                    viewModel.getUserList(listOf(newUser))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
            }
        })

        chatRoomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isFirstLoaded = true
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}