package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.lilliprovider.LilliContract
import com.lilli.gulliver.lilliprovider.LilliProvider
import com.github.kevinsawicki.http.HttpRequest
import org.json.JSONObject
import java.io.File

class LilliDownloader : Downloader {
    class object {
        private val APPNAME = "gulliver"
        private val PUBLICKEY = "10ea41f4-a80c-4ce2-95a2-1e04ca03ea60"
        public val CREDENTIALS : String = "credentials"

        fun getCredentials(context : Context?) : Map<String, String?> {
            val credentials = context?.getSharedPreferences(CREDENTIALS, Context.MODE_PRIVATE)

            val username = credentials?.getString(LilliContract.USERNAME, null)
            val password = credentials?.getString(LilliContract.PASSWORD, null)
            val auth = hashMapOf(Pair(LilliContract.USERNAME, username), Pair(LilliContract.PASSWORD, password))

            if (username == null || password == null) {
                val url = LilliProvider.ENDPOINT + "/edges"

                val request = HttpRequest.post(url)
                request?.basic(APPNAME, PUBLICKEY)

                if (request?.ok() == true) {
                    val response = JSONObject(request?.body())

                    val user = response.getString("public_key")
                    val pass = response.getString("shared_secret")

                    val editor = credentials?.edit()

                    if (user != null) {
                        auth.put(LilliContract.USERNAME, user)
                        editor?.putString(LilliContract.USERNAME, user)
                    }

                    if (pass != null) {
                        auth.put(LilliContract.PASSWORD, pass)
                        editor?.putString(LilliContract.PASSWORD, pass)
                    }

                    editor?.commit()
                }
            }

            return auth
        }
    }

    override fun getData(resource: String, context: Context?): File? {
        val resolver = context?.getContentResolver()
        val credentials = getCredentials(context)

        val username = credentials.get(LilliContract.USERNAME)
        val password = credentials.get(LilliContract.PASSWORD)

        val uri = LilliContract.Objects.CONTENT_URI
                .buildUpon()
               ?.appendPath(resource)
               ?.appendQueryParameter(LilliContract.USERNAME, username)
               ?.appendQueryParameter(LilliContract.PASSWORD, password)
               ?.appendQueryParameter(LilliContract.ALGORITHM, "gps") // just for now
               ?.build()

        val row = resolver?.query(uri, array(LilliContract.Objects.DATA), null, null, null)
        row?.moveToFirst()
        val path = row?.getString(0)
        if (path != null) {
            return File(path)
        }
        return null
    }

    public fun toString() : String {
        return "lilli"
    }
}