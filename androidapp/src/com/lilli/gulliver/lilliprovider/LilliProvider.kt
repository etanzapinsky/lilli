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
        private val strategies = mapOf("network" to NetworkStrategy)

        fun getFilename(uri : Uri?) : String {
            return "%s.tmp".format(uri?.getLastPathSegment())
        }
    }

    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    public override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, "objects", LilliContract.OBJECTS)
        sUriMatcher.addURI(AUTHORITY, "objects/*", LilliContract.OBJECTS_ID)
        sUriMatcher.addURI(AUTHORITY, "edges/*", LilliContract.EDGES_ID)

        return true
    }

    public override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val match = sUriMatcher.match(uri)
        val cursor = MatrixCursor(projection)

        if (match == LilliContract.OBJECTS_ID && projection?.size == 1 && projection?.get(0) == LilliContract.Objects.CACHED_DATA) {
            cursor.addRow(projection?.map { getCachedFile(uri) })
            return cursor
        }

        val request = buildRequestFromUri(uri, "GET")

        if (request.ok()) {
            val response = JSONObject(request.body())

            val row = when (match) {
                LilliContract.OBJECTS_ID -> projection?.map(objectsMap(uri, response))
                else -> projection?.map { null }
            }

            cursor.addRow(row)
        }

        return cursor
    }

    private fun objectsMap(uri: Uri?, response: JSONObject?): (String) -> Any? {
        return {
            (k) -> when (k) {
                LilliContract.Objects.ID -> getIdFromResponse(LilliContract.OBJECTS_ID, response)
                LilliContract.Objects.DATA -> getFile(uri, response)
                else -> response?.get(k)
            }
        }
    }

    private fun buildRequestFromUri(uri: Uri?, method: String): HttpRequest {
        val username = uri?.getQueryParameter(LilliContract.USERNAME)
        val password = uri?.getQueryParameter(LilliContract.PASSWORD)
        val algorithm = uri?.getQueryParameter(LilliContract.ALGORITHM)

        var builder = Uri.parse(ENDPOINT)?.buildUpon()?.path(uri?.getPath())
        if (algorithm != null) {
            builder = builder?.appendQueryParameter(LilliContract.ALGORITHM, algorithm)
        }
        val url = builder?.build().toString()

        val request = HttpRequest(url, method)
        request.basic(username, password)

        return request
    }

    private fun getFile(uri : Uri?, response: JSONObject?) : String? {
        var path : String? = null
        val context = getContext()
        val neighbors = response?.getJSONArray("neighbors")

        if (neighbors != null && neighbors.length() > 0) {
            val range = IntRange(0, neighbors.length())
            for (i in range) {
                val neighbor = neighbors.getJSONObject(i)
                val strategy = strategies[neighbor?.getString("connect_with")]
                path = strategy?.get(context, uri, neighbor)

                if (path != null) {
                    break
                }
            }
        }

        if (path == null) {
            path = AuthoritativeLocationStrategy.get(context, uri, response)
        }

        if (path != null) {
            update(uri, null, null, null)
        }

        return path
    }

    private fun getCachedFile(uri : Uri?) : String? {
        val file = getContext()?.getFileStreamPath(getFilename(uri))
        if (file?.exists() == true) {
            return file?.getPath()
        } else {
            return null
        }
    }

    private fun buildAttributeRequest(uri: Uri?, values: ContentValues?, method: String): HttpRequest {
        val request = buildRequestFromUri(uri, method)
        request.contentType("application/json")
        val output = JSONObject()

        if (values != null) {
            for (v in values.valueSet()?.iterator()) {
                output.put(v.getKey(), v.getValue())
            }
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