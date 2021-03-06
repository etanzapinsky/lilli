package com.lilli.gulliver.torrentprovider

import android.content.ContentProvider
import android.net.Uri
import android.content.ContentValues
import android.database.Cursor
import android.os.ParcelFileDescriptor
import android.content.UriMatcher
import com.turn.ttorrent.client.Client
import java.net.InetAddress
import android.util.Log
import android.database.MatrixCursor
import com.turn.ttorrent.client.SharedTorrent
import java.io.File
import com.github.kevinsawicki.http.HttpRequest
import java.util.concurrent.ConcurrentHashMap

class TorrentProvider : ContentProvider() {
    class object {
        val AUTHORITY = "com.lilli.gulliver.torrentprovider"
//        "content://com.lilli.gulliver.torrentprovider/path_to_.torrent"
    }

    val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    var seedingClients = ConcurrentHashMap<String, Client>()

    public override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, "*", TorrentContract.OBJECT_NAME)

        return true
    }

    public override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        Log.d(AUTHORITY, uri.toString())

        val cursor = MatrixCursor(projection)
        val torrentPath = uri?.getPath()
        if (torrentPath != null) {
            val torrentFile = File(torrentPath)
            val client = Client(getLocalIpAddress(),
                                SharedTorrent.fromFile(torrentFile, this.getContext()?.getFilesDir()))
            Log.d(AUTHORITY, getLocalIpAddress().toString())
            // when you download a file, you're not automatically going to start seeding it
            client.download()
            client.waitForCompletion()
            // when we're here file has finished downloading
            Log.d(AUTHORITY, "Got the file!")
            val row = when (sUriMatcher.match(uri)) {
                TorrentContract.OBJECT_NAME -> projection?.map(
                        {(k) ->
                            when (k) {
                                TorrentContract.Objects.ID -> client.getTorrent()?.getHexInfoHash()
                            // can assume filepath where torrent is downloaded to is the first file in the
                            // list of filenames since the torrents we will have will only have one file
                                TorrentContract.Objects.DATA -> getFile(client)
                                else -> null
                            }
                        })
                else -> projection?.map { null }
            }
            cursor.addRow(row)
        }
        return cursor
    }

    public override fun getType(uri: Uri?): String? {
        return when (sUriMatcher.match(uri)) {
            TorrentContract.OBJECT_NAME -> "vnd.android.cursor.item/vnd.com.lilli.gulliver.torrentprovider.torrent"
            else -> null
        }
    }

    public override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        val torrentPath = uri?.getPath()
        if (torrentPath != null) {
            val torrentFile = File(torrentPath)
            val client = Client(getLocalIpAddress(),
                    SharedTorrent.fromFile(torrentFile, this.getContext()?.getFilesDir()))
            seedingClients?.put(torrentPath, client)
            client?.share()
            return uri
        }
        return null
    }

    public override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val torrentPath = uri?.getPath()
        if (torrentPath != null) {
            val torrentFile = File(torrentPath)
            val client = seedingClients?.get(torrentPath)
            client?.stop()
            return 1
        }
        return 0
    }

    public override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    public override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? {
        return openFileHelper(uri, mode)
    }

    fun getLocalIpAddress() : InetAddress? {
        // set this up to return the requester's ip address
        val response = HttpRequest.get("http://lilli.etanzapinsky.com/")?.body()
        if (response != null) {
            return InetAddress.getByName(response)
        }

        return null
    }

    fun getFile(client : Client) : File? {
        val fileName = client.getTorrent()?.getFilenames()?.get(0)
        if (fileName != null) {
            return File(this.getContext()?.getFilesDir(), fileName)
        }
        return null
    }
}