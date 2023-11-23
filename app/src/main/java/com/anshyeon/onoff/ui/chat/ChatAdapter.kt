package com.anshyeon.onoff.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.databinding.ItemChatRoomInfoBinding

class ChatAdapter(
    private val clickListener: OnEnterButtonClickListener
) :
    ListAdapter<ChatRoom, ChatAdapter.ChatRoomItemViewHolder>(ChatRoomDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomItemViewHolder {
        return ChatRoomItemViewHolder.from(parent, clickListener)
    }

    override fun onBindViewHolder(holder: ChatRoomItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatRoomItemViewHolder(
        private val binding: ItemChatRoomInfoBinding,
        private val clickListener: OnEnterButtonClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatRoom) {
            binding.chatRoom = item
            binding.clickListener = clickListener
        }

        companion object {
            fun from(
                parent: ViewGroup,
                clickListener: OnEnterButtonClickListener
            ): ChatRoomItemViewHolder {
                return ChatRoomItemViewHolder(
                    ItemChatRoomInfoBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    clickListener
                )
            }
        }
    }
}

class ChatRoomDiffUtil : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem.chatRoomId == newItem.chatRoomId
    }

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem == newItem
    }
}