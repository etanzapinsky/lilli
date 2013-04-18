/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.lilli.gulliver

import android.content.Context
import com.github.kevinsawicki.http.HttpRequest
import java.io.File

class OriginDownloader() : Downloader {

    /**
     * Make sure to close input stream when done!
     */
    public override fun getData(resource : String, context : Context?) : File? {
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