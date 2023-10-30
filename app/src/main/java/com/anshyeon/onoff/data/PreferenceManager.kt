package com.anshyeon.onoff.data

import android.content.Context

class PreferenceManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "com.anshyeon.onoff.PREFERENCE_KEY",
        Context.MODE_PRIVATE
    )

    fun getString(key: String, defValue: String): String {
        return sharedPreferences.getString(key, defValue) ?: defValue
    }

    fun setGoogleIdToken(key: String, googleIdToken: String) {
        sharedPreferences.edit().putString(key, googleIdToken).apply()
    }
}