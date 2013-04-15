package com.lilli.gulliver

import java.io.InputStream
import android.content.Context
import java.net.InetAddress
import com.turn.ttorrent.client.Client
import com.turn.ttorrent.client.SharedTorrent
import java.io.File
import com.lilli.gulliver.torrentprovider.TorrentContract
import java.io.BufferedInputStream
import android.util.Log


/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/14/13
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 */
class TorrentDownloader : Downloader {
    val AUTHORITY = "com.lilli.gulliver.torrentprovider"

    override fun getData(resource: String, context: Context?): InputStream? {
        val resolver = context?.getContentResolver()
        val uri = TorrentContract.Objects.CONTENT_URI
            .buildUpon()
            ?.appendPath(resource)
            ?.build()
        val input_stream = resolver?.openInputStream(uri)

        if (input_stream != null) {
            Log.d(AUTHORITY, "made it here!")
            return BufferedInputStream(input_stream)
        }

        return null
    }
}