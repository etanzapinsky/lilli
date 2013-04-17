package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.lilliprovider.LilliContract
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.HashMap
import com.lilli.gulliver.lilliprovider.LilliProvider
import com.github.kevinsawicki.http.HttpRequest
import org.json.JSONObject

class LilliDownloader : Downloader {
    class object {
        val APPNAME = "gulliver"
        val PUBLICKEY = "10ea41f4-a80c-4ce2-95a2-1e04ca03ea60"
    }

    override fun getData(resource: String, context: Context?): InputStream? {
        val resolver = context?.getContentResolver()
        val username = id(context)?.get(LilliContract.USERNAME)
        val password = id(context)?.get(LilliContract.PASSWORD)
        val uri = LilliContract.Objects.CONTENT_URI
                .buildUpon()
        ?.appendPath(resource)
        ?.appendQueryParameter(LilliContract.USERNAME, username)
        ?.appendQueryParameter(LilliContract.PASSWORD, password)
        ?.build()

        val input_stream = resolver?.openInputStream(uri)

        if (input_stream != null) {
            return BufferedInputStream(input_stream)
        }

        return null
    }

    fun id(context : Context?) : HashMap<String, String>? {
        val username = context?.getSharedPreferences(LilliContract.USERNAME, Context.MODE_PRIVATE)
        val password = context?.getSharedPreferences(LilliContract.PASSWORD, Context.MODE_PRIVATE)
        val u = username?.getString(LilliContract.USERNAME, null)
        val p = password?.getString(LilliContract.PASSWORD, null)
        val hm = HashMap<String, String>()
        if (u == null || p == null) {
            val url = LilliProvider.ENDPOINT + "/edges"
            val request = HttpRequest(url, "POST")
            request.basic(APPNAME, PUBLICKEY)
            if (request.ok()) {
                val response = JSONObject(request.body())
                val user = response.getString("public_key")
                val pass = response.getString("shared_secret")
                val uEditor = username?.edit()
                val pEditor = password?.edit()
                if (user != null) {
                    hm.put(LilliContract.USERNAME, user)
                    uEditor?.putString(LilliContract.USERNAME, user)
                    uEditor?.commit()
                }
                if (pass != null) {
                    hm.put(LilliContract.PASSWORD, pass)
                    pEditor?.putString(LilliContract.PASSWORD, pass)
                    pEditor?.commit()
                }
            }
        }
        else {
            hm.put(LilliContract.USERNAME, u)
            hm.put(LilliContract.PASSWORD, p)
        }
        return hm
    }
}