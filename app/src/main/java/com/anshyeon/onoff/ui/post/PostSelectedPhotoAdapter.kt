package com.anshyeon.onoff.ui.post

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.data.model.ImageContent
import com.anshyeon.onoff.databinding.ItemSelectedPhotoBinding

class PostSelectedPhotoAdapter(
    private val limit: Int,
    private val clickListener: PhotoRemoverOnclickListener
) : RecyclerView.Adapter<PostSelectedPhotoAdapter.ImageContentViewHolder>() {

    private val items = mutableListOf<ImageContent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageContentViewHolder {
        return ImageContentViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ImageContentViewHolder, position: Int) {
        holder.bind(position, items[position], clickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItems(): List<ImageContent> {
        return items
    }

    fun addImage(uri: Uri, fileName: String) {
        val position = items.size
        if (position < limit) {
            items.add(ImageContent(uri, fileName))
            notifyItemInserted(position)
        }
    }

    fun removeImage(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }


    class ImageContentViewHolder(private val binding: ItemSelectedPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, item: ImageContent, updateListener: PhotoRemoverOnclickListener) {
            with(binding) {
                position = pos
                imageContent = item
                clickListener = updateListener
            }
        }

        companion object {
            fun from(parent: ViewGroup): ImageContentViewHolder {
                return ImageContentViewHolder(
                    ItemSelectedPhotoBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
}