package com.lilli.gulliver

import android.content.Context
import com.github.kevinsawicki.http.HttpRequest
import java.io.File

object OriginDownloader : Downloader {
    public override fun getData(resource : String, context : Context?, options : Map<String, String?>?) : File? {
        val request = HttpRequest.get(resource)
        val resourceArr = resource.split('/')
        val filename = resourceArr.get(resourceArr.size-1)
        val file = File(context?.getCacheDir(), filename)
        file.writeBytes(request?.buffer()?.readBytes())
        return file
    }

    public fun toString() : String {
        return "origin"
    }
}