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

class OriginDownloader<String, Integer, Long>(param : Context) : Downloader<String, Integer, Long>() {
    val context = param

    public override fun ready() : Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.getActiveNetworkInfo()
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
    protected override fun getData(resource : String) : InputStream? {
        val input : InputStream?
        val url : URL = URL(resource as jet.String)
        val conn : HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.setReadTimeout(10000 /* milliseconds */)
        conn.setConnectTimeout(15000 /* milliseconds */)
        conn.setRequestMethod("GET")
        conn.setDoInput(true)
        // Starts the query
        conn.connect()
        val response : Int = conn.getResponseCode()
        Log.d("OriginDownloader", "The response is: " + response)
        input = conn.getInputStream()
        return input
    }


    /**
     * For now we are going to assume the downloader is only passed one url at a time.
     * It wouldn't be hard to extend it to take multiple urls at once, check out the example here:
     * http://developer.android.com/reference/android/os/AsyncTask.html
     */


    protected override fun doInBackground(vararg resources : String?) : Long {
        this.getData(resources.get(0))
        val l = 1
        return l as Long
    }

    protected override fun onPostExecute(result : Long?) {
        //        showDialog("Downloaded " + result + " bytes")
    }

}