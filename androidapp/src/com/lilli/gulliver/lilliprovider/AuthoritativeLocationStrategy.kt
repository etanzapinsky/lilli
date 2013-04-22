package com.lilli.gulliver.lilliprovider

import android.content.Context
import org.json.JSONObject
import com.github.kevinsawicki.http.HttpRequest
import android.net.Uri
import android.util.Log

object AuthoritativeLocationStrategy : LilliStrategy {
    override fun get(context : Context?, uri: Uri?, response : JSONObject?): String? {
        Log.d("AuthoritativeLocationStrategy", "DOING IT")
        val authoritative_location = response?.getString(LilliContract.Objects.AUTHORITATIVE_LOCATION)
        val filename = LilliProvider.getFilename(uri)
        val fos = context?.openFileOutput(filename, Context.MODE_PRIVATE)

        HttpRequest.get(authoritative_location)?.receive(fos)

        return context?.getFileStreamPath(filename)?.getPath()
    }
}