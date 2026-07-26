package com.arbonvata.pollentracker

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PollenTrackerApplication :
    Application(),
    SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader
            .Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }.build()
}
