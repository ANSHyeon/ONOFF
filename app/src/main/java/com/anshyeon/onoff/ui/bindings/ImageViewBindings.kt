package com.anshyeon.onoff.ui.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import com.anshyeon.onoff.R

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
        view.load(url) {
            crossfade(true)
            placeholder(R.drawable.shape_grey_rounded_rectangle)
            error(R.drawable.ic_profile)
        }
    } else {
        view.setImageResource(R.drawable.ic_profile)
    }
}