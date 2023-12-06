package com.anshyeon.onoff.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anshyeon.onoff.databinding.ViewNetworkErrorBinding

class NetworkErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewNetworkErrorBinding

    init {
        binding = ViewNetworkErrorBinding.inflate(LayoutInflater.from(context), this)
    }
}