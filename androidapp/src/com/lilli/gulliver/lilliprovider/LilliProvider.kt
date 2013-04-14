package com.lilli.gulliver.provider

import android.content.ContentProvider
import android.net.Uri
import android.database.Cursor
import android.content.ContentValues
import android.os.ParcelFileDescriptor
import android.database.MatrixCursor
import com.github.kevinsawicki.http.HttpRequest
import android.content.UriMatcher
import org.json.JSONObject
import android.util.Log
import com.lilli.gulliver.lilliprovider.LilliContract

class LilliProvider : ContentProvider() {
    class object {
        val ENDPOINT = "http://test:b4189a15-450e-419e-9b34-042320c72cc8@lilli.etanzapinsky.com"
        val AUTHORITY = "com.lilli.gulliver.lilliprovider"
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

        Log.d(AUTHORITY, request.url().toString())

        if (request.ok()) {
            Log.d(AUTHORITY, "Request was OK!")
            val response = JSONObject(request.body())

            val row = when (sUriMatcher.match(uri)) {
                LilliContract.OBJECTS_ID -> projection?.map(objectsMap(response))
                else -> projection?.map { null }
            }

            Log.d(AUTHORITY, "Matcher was: %d".format(sUriMatcher.match(uri)))

            cursor.addRow(row)
        }

        return cursor
    }

    fun objectsMap(response: JSONObject?): (String) -> Any? {
        val f : (String) -> Any? = {
            (k) -> {
                val value = response?.get(k)
                when (value) {
                    LilliContract.Objects.ID -> response?.getString("public_key")
                    LilliContract.Objects.DATA -> getFile(response)
                    else -> value
                }
            }
        }
        return f
    }

    fun buildRequestFromUri(uri: Uri?, method: String): HttpRequest {
        val url = Uri.parse(ENDPOINT)
                ?.buildUpon()
                ?.path(uri?.getPath())
                ?.build()
                 .toString()

        val userinfo = uri?.getUserInfo()?.split(":")
        val username = userinfo?.get(0)
        val password = userinfo?.get(1)

        val request = HttpRequest(url, method)
//        request.basic(username, password)

        return request
    }

    fun getFile(response: JSONObject?) : String? {
        return HttpRequest.get(response?.getString(LilliContract.Objects.AUTHORITATIVE_LOCATION))?.body()
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
            LilliContract.OBJECTS -> "vnd.android.cursor.dir/vnd.com.lilli.gulliver.lilliprovider.objects"
            LilliContract.OBJECTS_ID -> "vnd.android.cursor.item/vnd.com.lilli.gulliver.lilliprovider.objects"
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