package com.anshyeon.onoff.ui.chatRoom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.databinding.ItemFirstReceiveMessageBinding
import com.anshyeon.onoff.databinding.ItemReceiveMessageBinding
import com.anshyeon.onoff.databinding.ItemSendMessageBinding

private const val VIEW_TYPE_FIRST_RECEIVE_MESSAGE = 0
private const val VIEW_TYPE_RECEIVE_MESSAGE = 1
private const val VIEW_TYPE_SEND_MESSAGE = 2

class ChatRoomAdapter(private val viewModel: ChatRoomViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Message>()

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
                val item = items[position]
                holder.bind(item)
            }

            is ReceiveMessageViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is SendMessageViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        val previousSender = if (position < 1) "" else items[position - 1].sender.email
        return if (viewModel.currentUser?.email == item.sender.email) {
            VIEW_TYPE_SEND_MESSAGE
        } else if (item.sender.email == previousSender) {
            VIEW_TYPE_RECEIVE_MESSAGE
        } else {
            VIEW_TYPE_FIRST_RECEIVE_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitMessageList(messages: List<Message>) {
        val currentSize = itemCount
        items.addAll(messages)
        notifyItemRangeInserted(currentSize, messages.size)
    }

    class FirstReceiveMessageViewHolder(private val binding: ItemFirstReceiveMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            binding.message = item
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