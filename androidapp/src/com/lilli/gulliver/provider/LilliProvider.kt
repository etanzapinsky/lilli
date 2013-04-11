package com.lilli.gulliver.provider

import android.content.ContentProvider
import android.net.Uri
import android.database.Cursor
import android.content.ContentValues
import android.os.ParcelFileDescriptor
import android.database.MatrixCursor
import com.github.kevinsawicki.http.HttpRequest
import android.content.ContentUris
import android.content.UriMatcher
import org.json.JSONObject
import org.json.JSONArray
import android.util.Log

class LilliProvider : ContentProvider() {
    class object {
        val ENDPOINT = "http://lilli.etanzapinsky.com"
        val AUTHORITY = "com.lilli.gulliver.provider"
    }

    val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    public override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, "objects", LilliContract.OBJECTS)
        sUriMatcher.addURI(AUTHORITY, "objects/#", LilliContract.OBJECTS_ID)

        return true
    }

    public override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        Log.d(AUTHORITY, uri.toString())

        val cursor = MatrixCursor(projection)
        val request = buildRequestFromUri(uri, "GET")

        if (request.ok()) {
            Log.d(AUTHORITY, "Request was OK!")
            val response = JSONObject(request.body())

            val row = when (sUriMatcher.match(uri)) {
                LilliContract.OBJECTS_ID -> projection?.map(objectsMap(response, uri))
                else -> projection?.map { null }
            }

            Log.d(AUTHORITY, "Matcher was: %d".format(sUriMatcher.match(uri)))

            cursor.addRow(row)
        }

        return cursor
    }

    fun objectsMap(response: JSONObject?, uri: Uri?): (String) -> Any? {
        val f : (String) -> Any? = {
            (k) -> {
                val value = response?.get(k)
                when (value) {
                    LilliContract.Objects.ID -> uri?.getLastPathSegment()
                    LilliContract.Objects.DATA -> getFile(value as? JSONArray)
                    else -> value
                }
            }
        }
        return f
    }

    fun buildRequestFromUri(uri: Uri?, method: String): HttpRequest {
        val url = Uri.parse(ENDPOINT)
                ?.buildUpon()
                ?.appendEncodedPath(uri?.getEncodedPath())
                ?.build()
                 .toString()

        val userinfo = uri?.getUserInfo()?.split(":")
        val username = userinfo?.get(0)
        val password = userinfo?.get(1)

        val request = HttpRequest(url, method)
        request.basic(username, password)

        return request
    }

    fun getFile(neighbors: JSONArray?) : String? {
        return null
    }

    fun buildAttributeRequest(uri: Uri?, values: ContentValues?, method: String): HttpRequest {
        val request = buildRequestFromUri(uri, method)
        val output = JSONObject()

        for (v in values?.valueSet()?.iterator()) {
            output.put(v.getKey(), v.getValue())
        }

        request.send(output.toString())

        return request
    }

    public override fun getType(uri: Uri?): String? {
        return when (sUriMatcher.match(uri)) {
            LilliContract.OBJECTS -> "vnd.android.cursor.dir/vnd.com.lilli.gulliver.provider.objects"
            LilliContract.OBJECTS_ID -> "vnd.android.cursor.item/vnd.com.lilli.gulliver.provider.objects"
            else -> null
        }
    }

    fun deriveIdFromUriAndResponse(uri: Uri?, response: JSONObject): String? {
        return when (sUriMatcher.match(uri)) {
            LilliContract.OBJECTS -> response.getString("public_key")
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