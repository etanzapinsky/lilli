package com.lilli.gulliver

import android.content.Context
import com.lilli.gulliver.torrentprovider.TorrentContract
import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/14/13
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 */
class TorrentDownloader : Downloader {
    val AUTHORITY = "com.lilli.gulliver.torrentprovider"

    override fun getData(resource: String, context: Context?): File? {
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