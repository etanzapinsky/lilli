/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.example.androidapp

import java.net.URL
import android.net.ConnectivityManager
import android.content.Context
import android.net.NetworkInfo
import android.util.Log
import java.io.InputStream
import java.net.HttpURLConnection

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
     public override fun getData(resource : String) : InputStream? {
        val url : URL = URL(resource)
        val conn = url.openConnection() as? HttpURLConnection
        conn?.setReadTimeout(10000) /* milliseconds */
        conn?.setConnectTimeout(15000) /* milliseconds */
        conn?.setRequestMethod("GET")
        conn?.setDoInput(true)
        // Starts the query
        conn?.connect()
        val response = conn?.getResponseCode()
        Log.d("OriginDownloader", "The response is: " + response)
        return conn?.getInputStream()
    }

    public override fun getDownloadTime() : Int {
        return 1
    }
}