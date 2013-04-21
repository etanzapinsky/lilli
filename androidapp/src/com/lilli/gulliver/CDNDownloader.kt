package com.lilli.gulliver

import android.content.Context
import java.io.File

object CDNDownloader : Downloader {
    override fun getData(resource: String, context: Context?, options: Map<String, String?>?): File? {
        return OriginDownloader.getData(resource, context, options)
    }

    public fun toString() : String {
        return "cdn"
    }
}