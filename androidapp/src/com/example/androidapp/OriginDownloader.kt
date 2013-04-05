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
import android.util.Log
import java.net.HttpURLConnection
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.File

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
    public override fun getData(resource : String, context : Context?) : File? {
        val url : URL = URL(resource)
        val conn = url.openConnection() as? HttpURLConnection
        //            conn?.setReadTimeout(10000) /* milliseconds */
        //            conn?.setConnectTimeout(15000) /* milliseconds */
        conn?.setRequestMethod("GET")
        conn?.setDoInput(true)
        // Starts the query
        conn?.connect()
        val response = conn?.getResponseCode()
        val lengthOfFile = conn?.getContentLength()
        val urlFileName = url.getFile()
        if (urlFileName != null && context != null) {
            val pathArr = urlFileName.split("/")
            var fileName = pathArr.get(pathArr.size - 1)
            val outputFile = File(context.getCacheDir(), fileName)
            Log.d("OriginDownloader", "The response is: " + response)
            Log.d("OriginDownloader", "File length: " + lengthOfFile)
            val urlStream = url.openStream()
            if (urlStream != null) {
                val inputStream = BufferedInputStream(urlStream)
                val outputStream = FileOutputStream(outputFile)
                var data = ByteArray(1024)
                var count = inputStream.read(data)
                while (count != -1) {
                    outputStream.write(data, 0, count)
                    count = inputStream.read(data)
//                    Log.d("OriginDownloader", "Read: " + count + " bytes")
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            }
            return outputFile
        }
        return null
    }
}