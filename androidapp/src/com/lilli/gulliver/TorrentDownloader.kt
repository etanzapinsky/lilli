package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.torrentprovider.TorrentContract
import java.io.File

object TorrentDownloader : Downloader {
    val AUTHORITY = "com.lilli.gulliver.torrentprovider"

    public override fun getData(resource: String, context: Context?, options: Map<String, String?>?): File? {
        val resolver = context?.getContentResolver()
        val uri = TorrentContract.Objects.CONTENT_URI
            .buildUpon()
            ?.appendPath(resource)
            ?.build()

        val row = resolver?.query(uri, array(TorrentContract.Objects.DATA), null, null, null)
        row?.moveToFirst()
        val path = row?.getString(0)
        if (path != null) {
            return File(path)
        }
        return null
    }

    public fun toString() : String {
        return "bittorent"
    }
}