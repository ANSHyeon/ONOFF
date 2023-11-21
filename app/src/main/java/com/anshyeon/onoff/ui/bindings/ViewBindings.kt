package com.anshyeon.onoff.ui.bindings

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visible")
fun isVisible(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}