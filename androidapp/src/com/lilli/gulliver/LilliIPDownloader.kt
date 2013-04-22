package com.lilli.gulliver

import android.content.Context
import java.io.File

object LilliIPDownloader : Downloader {
    override fun getData(resource: String, context: Context?, options: Map<String, String?>?): File? {
        return LilliDownloader.getData(resource, context, options)
    }

    public fun toString(): String {
        return "lilli_ip"
    }
}
