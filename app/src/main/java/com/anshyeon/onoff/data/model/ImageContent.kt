package com.anshyeon.onoff.data.model

import android.net.Uri
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageContentHeader(
    val size: Int,
    val limit: Int
)

@JsonClass(generateAdapter = true)
data class ImageContent(
    val uri: Uri,
    val fileName: String
)