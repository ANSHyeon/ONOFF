package com.anshyeon.onoff.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anshyeon.onoff.databinding.ItemViewpagerImageBinding

class DetailPagerStateAdapter(
    private val urlList: List<String>
) : RecyclerView.Adapter<DetailPagerStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemViewpagerImageBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(urlList[position])
    }

    override fun getItemCount() = urlList.size

    class ViewHolder(private val binding: ItemViewpagerImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            binding.url = url
        }
    }
}