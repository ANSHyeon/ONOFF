package com.anshyeon.onoff.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anshyeon.onoff.databinding.ViewPlaceInfoBinding

class PlaceInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewPlaceInfoBinding

    init {
        binding = ViewPlaceInfoBinding.inflate(LayoutInflater.from(context), this)
    }

    fun setPlaceName(placeName: String?) {
        binding.tvPlaceName.text = placeName
    }

    fun setAddress(address: String?) {
        binding.tvPlaceAdress.text = address
    }

    fun setClickListener(operation: () -> Unit) {
        binding.btnEnterChatRoom.setOnClickListener {
            operation()
        }
    }

    fun setButtonText(text: String) {
        binding.btnEnterChatRoom.text = text
    }
}