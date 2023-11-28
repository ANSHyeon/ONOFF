package com.anshyeon.onoff.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.data.model.ImageContentHeader
import com.anshyeon.onoff.databinding.ItemPhotoSelectorBinding

class PostPhotoSelectorAdapter(
    private val clickListener: PhotoSelectorOnclickListener
) : RecyclerView.Adapter<PostPhotoSelectorAdapter.ImageHeaderViewHolder>() {

    private val items = mutableListOf<ImageContentHeader>()
    private var itemLimit = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHeaderViewHolder {
        return ImageHeaderViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ImageHeaderViewHolder, position: Int) {
        holder.bind(items[position], clickListener)
    }

    fun setImageHeader(limit: Int) {
        itemLimit = limit
        items.add(ImageContentHeader(0, limit))
        notifyItemInserted(0)
    }

    fun updateImageHeader(currentSize: Int) {
        items[0] = ImageContentHeader(currentSize, itemLimit)
        notifyItemChanged(0)
    }

    class ImageHeaderViewHolder(private val binding: ItemPhotoSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: ImageContentHeader, clickListener: PhotoSelectorOnclickListener) {
            binding.header = header
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ImageHeaderViewHolder {
                return ImageHeaderViewHolder(
                    ItemPhotoSelectorBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
}