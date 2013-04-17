package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.lilliprovider.LilliContract
import java.io.BufferedInputStream
import java.io.InputStream

class LilliDownloader : Downloader {
    class object {
        val USERNAME = "00844321-16f0-4f87-a49b-94d42c5b693b"
        val PASSWORD = "2ca55bb0-c7e2-4302-9994-6b6e34c9116c"
    }

    override fun getData(resource: String, context: Context?): InputStream? {
        val resolver = context?.getContentResolver()
        val uri = LilliContract.Objects.CONTENT_URI
                .buildUpon()
               ?.appendPath(resource)
               ?.appendQueryParameter(LilliContract.USERNAME, USERNAME)
               ?.appendQueryParameter(LilliContract.PASSWORD, PASSWORD)
               ?.build()

        val input_stream = resolver?.openInputStream(uri)

        if (input_stream != null) {
            return BufferedInputStream(input_stream)
        }

        return null
    }
}