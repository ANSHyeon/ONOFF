package com.anshyeon.onoff.ui.chatRoom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.databinding.ItemFirstReceiveMessageBinding
import com.anshyeon.onoff.databinding.ItemReceiveMessageBinding
import com.anshyeon.onoff.databinding.ItemSendMessageBinding

private const val VIEW_TYPE_FIRST_RECEIVE_MESSAGE = 0
private const val VIEW_TYPE_RECEIVE_MESSAGE = 1
private const val VIEW_TYPE_SEND_MESSAGE = 2

class ChatRoomAdapter :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private lateinit var currentUserId: String
    private lateinit var userList: List<User>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FIRST_RECEIVE_MESSAGE -> FirstReceiveMessageViewHolder.from(parent)
            VIEW_TYPE_RECEIVE_MESSAGE -> ReceiveMessageViewHolder.from(parent)
            else -> SendMessageViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FirstReceiveMessageViewHolder -> {
                val item = getItem(position)
                holder.bind(item, userList)
            }

            is ReceiveMessageViewHolder -> {
                val item = getItem(position)
                holder.bind(item)
            }

            is SendMessageViewHolder -> {
                val item = getItem(position)
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val previousSender = if (position < 1) "" else getItem(position - 1).userId
        return if (currentUserId == item.userId) {
            VIEW_TYPE_SEND_MESSAGE
        } else if (item.userId == previousSender) {
            VIEW_TYPE_RECEIVE_MESSAGE
        } else {
            VIEW_TYPE_FIRST_RECEIVE_MESSAGE
        }
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun setUserList(localUserList: List<User>) {
        userList = localUserList
    }

    class FirstReceiveMessageViewHolder(private val binding: ItemFirstReceiveMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message, userList: List<User>) {
            val user = userList.filter { user -> user.userId == item.userId }
            if (user.isNotEmpty()) {
                binding.message = item.copy(
                    nickName = user.first().nickName,
                    profileUrl = user.first().profileUrl
                )
            }
        }

        companion object {
            fun from(parent: ViewGroup): FirstReceiveMessageViewHolder {
                return FirstReceiveMessageViewHolder(
                    ItemFirstReceiveMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    class ReceiveMessageViewHolder(private val binding: ItemReceiveMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            binding.message = item
        }

        companion object {
            fun from(parent: ViewGroup): ReceiveMessageViewHolder {
                return ReceiveMessageViewHolder(
                    ItemReceiveMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    class SendMessageViewHolder(private val binding: ItemSendMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            binding.message = item
        }

        companion object {
            fun from(parent: ViewGroup): SendMessageViewHolder {
                return SendMessageViewHolder(
                    ItemSendMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}