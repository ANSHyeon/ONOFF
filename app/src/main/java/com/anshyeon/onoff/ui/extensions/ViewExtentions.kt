package com.anshyeon.onoff.ui.extensions

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun View.showMessage(@StringRes messageId: Int) {
    Snackbar.make(this, messageId, Snackbar.LENGTH_LONG)
        .setAnchorView(this)
        .show()
}

fun View.setClickEvent(
    uiScope: CoroutineScope,
    windowDuration: Long = 3000,
    onClick: () -> Unit,
) {
    clicks()
        .throttleFist(windowDuration)
        .onEach { onClick.invoke() }
        .launchIn(uiScope)
}