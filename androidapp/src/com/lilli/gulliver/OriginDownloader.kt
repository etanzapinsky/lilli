/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.lilli.gulliver

import android.content.Context
import android.util.Log
import com.github.kevinsawicki.http.HttpRequest
import java.io.InputStream

class OriginDownloader() : Downloader {

    /**
     * Make sure to close input stream when done!
     */
    public override fun getData(resource : String, context : Context?) : InputStream? {
        return HttpRequest.get(resource)?.buffer()
    }
}