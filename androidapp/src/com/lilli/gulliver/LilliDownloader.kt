package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.lilliprovider.LilliContract
import java.io.BufferedInputStream
import java.io.InputStream

class LilliDownloader : Downloader {
    override fun ready(): Boolean {
        return true
    }

    override fun getData(resource: String, context: Context?): InputStream? {
        val resolver = context?.getContentResolver()
        val uri = LilliContract.Objects.CONTENT_URI
                .buildUpon()
               ?.appendPath(resource)
               ?.build()

        val input_stream = resolver?.openInputStream(uri)

        if (input_stream != null) {
            return BufferedInputStream(input_stream)
        }

        return null
    }
}