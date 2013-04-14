package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.lilliprovider.LilliContract
import java.io.BufferedInputStream
import java.io.InputStream
import android.util.Log

class LilliDownloader : Downloader {
    override fun getData(resource: String, context: Context?): InputStream? {
        val resolver = context?.getContentResolver()
        val uri = LilliContract.Objects.CONTENT_URI
                .buildUpon()
               ?.appendPath(resource)
               ?.build()

        Log.d("com.lilli.gulliver.LilliDownloader", uri.toString())

        val input_stream = resolver?.openInputStream(uri)

        if (input_stream != null) {
            return BufferedInputStream(input_stream)
        }

        return null
    }
}