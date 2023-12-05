package com.anshyeon.onoff.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleCoroutineScope
import com.anshyeon.onoff.databinding.ViewPlaceInfoBinding
import com.anshyeon.onoff.ui.extensions.setClickEvent

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

    fun setClickListener(lifecycleScope: LifecycleCoroutineScope, operation: () -> Unit) {
        binding.btnEnterChatRoom.setClickEvent(lifecycleScope) {
            operation()
        }
    }

    fun setButtonText(text: String) {
        binding.btnEnterChatRoom.text = text
    }
}