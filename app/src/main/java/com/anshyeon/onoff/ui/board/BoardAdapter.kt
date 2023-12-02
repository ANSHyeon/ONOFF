package com.anshyeon.onoff.ui.board

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.data.model.Post
import com.anshyeon.onoff.databinding.ItemPostBinding

class BoardAdapter(
    private val clickListener: OnPostClickListener
) : ListAdapter<Post, BoardAdapter.PostItemViewHolder>(PostDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemViewHolder {
        return PostItemViewHolder.from(parent, clickListener)
    }

    override fun onBindViewHolder(holder: PostItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostItemViewHolder(
        private val binding: ItemPostBinding,
        private val clickListener: OnPostClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Post) {
            binding.post = item
            binding.clickListener = clickListener
        }

        companion object {
            fun from(
                parent: ViewGroup,
                clickListener: OnPostClickListener
            ): PostItemViewHolder {
                return PostItemViewHolder(
                    ItemPostBinding.inflate(
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

class PostDiffUtil : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.postId == newItem.postId
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}