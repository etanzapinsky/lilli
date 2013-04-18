package com.lilli.gulliver.lilliprovider

import android.content.ContentProvider
import android.net.Uri
import android.database.Cursor
import android.content.ContentValues
import android.os.ParcelFileDescriptor
import android.database.MatrixCursor
import com.github.kevinsawicki.http.HttpRequest
import android.content.UriMatcher
import org.json.JSONObject
import android.content.Context

class LilliProvider : ContentProvider() {
    class object {
        val ENDPOINT = "http://lilli.etanzapinsky.com"
        private val AUTHORITY = "com.lilli.gulliver.lilliprovider"
    }

    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    public override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, "objects", LilliContract.OBJECTS)
        sUriMatcher.addURI(AUTHORITY, "objects/*", LilliContract.OBJECTS_ID)
        sUriMatcher.addURI(AUTHORITY, "edges/*", LilliContract.EDGES_ID)

        return true
    }

    public override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val cursor = MatrixCursor(projection)
        val request = buildRequestFromUri(uri, "GET")

        if (request.ok()) {
            val response = JSONObject(request.body())

            val row = when (sUriMatcher.match(uri)) {
                LilliContract.OBJECTS_ID -> projection?.map(objectsMap(response))
                else -> projection?.map { null }
            }

            cursor.addRow(row)
        }

        return cursor
    }

    private fun objectsMap(response: JSONObject?): (String) -> Any? {
        return {
            (k) -> when (k) {
                LilliContract.Objects.ID -> getIdFromResponse(LilliContract.OBJECTS_ID, response)
                LilliContract.Objects.DATA -> getFile(response)
                else -> response?.get(k)
            }
        }
    }

    private fun buildRequestFromUri(uri: Uri?, method: String): HttpRequest {
        val url = Uri.parse(ENDPOINT)
                ?.buildUpon()
                ?.path(uri?.getPath())
                ?.build()
                 .toString()

        val username = uri?.getQueryParameter(LilliContract.USERNAME)
        val password = uri?.getQueryParameter(LilliContract.PASSWORD)

        val request = HttpRequest(url, method)
        request.basic(username, password)

        return request
    }

    private fun getFile(response: JSONObject?) : String? {
        val context = getContext()
        val authoritative_location = response?.getString(LilliContract.Objects.AUTHORITATIVE_LOCATION)
        val filename = "%d.tmp".format(authoritative_location?.hashCode())
        val fos = context?.openFileOutput(filename, Context.MODE_PRIVATE)

        HttpRequest.get(authoritative_location)?.receive(fos)

        return context?.getFileStreamPath(filename)?.getPath()
    }

    private fun buildAttributeRequest(uri: Uri?, values: ContentValues?, method: String): HttpRequest {
        val request = buildRequestFromUri(uri, method)
        request.contentType("application/json")
        val output = JSONObject()

        for (v in values?.valueSet()?.iterator()) {
            output.put(v.getKey(), v.getValue())
        }

        request.send(output.toString())

        return request
    }

    public override fun getType(uri: Uri?): String? {
        return when (sUriMatcher.match(uri)) {
            LilliContract.OBJECTS -> "vnd.android.cursor.dir/vnd.com.lilli.gulliver.lilliprovider.objects"
            LilliContract.OBJECTS_ID -> "vnd.android.cursor.item/vnd.com.lilli.gulliver.lilliprovider.objects"
            LilliContract.EDGES_ID -> "vnd.android.cursor.item/vnd.com.lilli.gulliver.lilliprovider.edges"
            else -> null
        }
    }

    private fun deriveIdFromUriAndResponse(uri: Uri?, response: JSONObject): String? {
        return getIdFromResponse(sUriMatcher.match(uri), response)
    }

    private fun getIdFromResponse(i: Int, response: JSONObject?): String? {
        return when (i) {
            LilliContract.OBJECTS, LilliContract.OBJECTS_ID -> response?.getString("public_key")
            else -> null
        }
    }

    public override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        val request = buildAttributeRequest(uri, values, "POST")

        if (request.ok()) {
            val response = JSONObject(request.body())
            val id = deriveIdFromUriAndResponse(uri, response)
            if (id != null) {
                return uri?.buildUpon()?.appendPath(id)?.build()
            }
        }

        return null
    }

    public override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val request = buildRequestFromUri(uri, "DELETE")

        if (request.ok()) {
            return 1
        } else {
            return 0
        }
    }

    public override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val request = buildAttributeRequest(uri, values, "PUT")

        if (request.ok()) {
            return 1
        } else {
            return 0
        }
    }

    public override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? {
        return openFileHelper(uri, mode)
    }

}