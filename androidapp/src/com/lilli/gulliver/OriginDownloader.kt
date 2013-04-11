/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.lilli.gulliver

import android.net.ConnectivityManager
import android.content.Context
import android.util.Log
import com.github.kevinsawicki.http.HttpRequest
import java.io.InputStream

class OriginDownloader(val context : Context) : Downloader {

    public override fun ready() : Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connMgr?.getActiveNetworkInfo()
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d("Downloader", "Connected!")
            return true
        } else {
            Log.d("Downloader", "Not Connected!")
            return false
        }
    }

    /**
     * Make sure to close input stream when done!
     */
    public override fun getData(resource : String, context : Context?) : InputStream? {
        return HttpRequest.get(resource)?.buffer()
    }
}