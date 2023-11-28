package com.anshyeon.onoff.ui.detail

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentDetailBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator

class DetailFragment : BaseFragment<FragmentDetailBinding>(R.layout.fragment_detail) {
    private val args: DetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.post = args.post
        setViewPager()
        setNavigationOnClickListener()
    }

    private fun setViewPager() {
        val adapter = DetailPagerStateAdapter(args.post.imageUrlList)
        binding.viewpagerDetailImage.adapter = adapter
        TabLayoutMediator(
            binding.viewpagerDetailImageIndicator,
            binding.viewpagerDetailImage
        ) { tab, position ->

        }.attach()
    }

    private fun setNavigationOnClickListener() {
        binding.detailAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}