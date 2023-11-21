package com.anshyeon.onoff.util

import android.content.res.Resources
import java.util.Locale

object SystemConfiguration {

    val currentLocale: Locale
        get() = Resources.getSystem().configuration.locales.get(0)
}