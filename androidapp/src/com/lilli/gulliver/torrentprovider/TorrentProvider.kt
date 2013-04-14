package com.lilli.gulliver.torrentprovider

import android.content.ContentProvider
import android.net.Uri
import android.content.ContentValues
import android.database.Cursor
import android.os.ParcelFileDescriptor
import android.content.UriMatcher
import com.turn.ttorrent.client.Client

class TorrentProvider : ContentProvider() {
    class object {
        val ENDPOINT = "http://lilli.etanzapinsky.com:6969"
        val AUTHORITY = "com.lilli.gulliver.torrentprovider"
    }

    val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    public override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, "objects", TorrentContract.OBJECTS)
        sUriMatcher.addURI(AUTHORITY, "objects/#", TorrentContract.OBJECTS_ID)

        return true
    }

    public override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
    }

    public override fun getType(uri: Uri?): String? {
        return when (sUriMatcher.match(uri)) {
            TorrentContract.OBJECTS -> "vnd.android.cursor.dir/vnd.com.lilli.gulliver.torrentprovider.objects"
            TorrentContract.OBJECTS_ID -> "vnd.android.cursor.item/vnd.com.lilli.gulliver.torrentprovider.objects"
            else -> null
        }
    }

    public override fun insert(uri: Uri?, values: ContentValues?): Uri? {
    }

    public override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
    }

    public override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
    }

    public override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? {
    }

}