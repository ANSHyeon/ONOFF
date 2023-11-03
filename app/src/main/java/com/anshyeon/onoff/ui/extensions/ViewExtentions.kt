package com.anshyeon.onoff.ui.extensions

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun View.showMessage(@StringRes messageId: Int) {
    Snackbar.make(this, messageId, Snackbar.LENGTH_LONG)
        .setAnchorView(this)
        .show()
}