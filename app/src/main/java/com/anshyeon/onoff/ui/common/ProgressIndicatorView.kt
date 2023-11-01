package com.anshyeon.onoff.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anshyeon.onoff.databinding.ViewProgressIndicatorBinding

class ProgressIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    init {
        ViewProgressIndicatorBinding.inflate(LayoutInflater.from(context), this)
        setOnKeyListener { a,keyCode,event ->
            return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        }
    }
}