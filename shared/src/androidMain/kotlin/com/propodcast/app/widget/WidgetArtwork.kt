package com.propodcast.app.widget

import android.content.Context
import android.graphics.Bitmap
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap

/**
 * Loads the artwork as a small software bitmap through the app's Coil loader (so it is usually cached):
 * widgets are drawn by the launcher from RemoteViews, which can't take hardware bitmaps.
 */
internal suspend fun loadWidgetArtwork(context: Context, url: String): Bitmap? {
    val request = ImageRequest.Builder(context)
        .data(url)
        .size(ARTWORK_SIZE_PX)
        .allowHardware(false)
        .build()
    val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
    return result.image.toBitmap()
}

private const val ARTWORK_SIZE_PX = 192
