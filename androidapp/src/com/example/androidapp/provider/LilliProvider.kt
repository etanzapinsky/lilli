package com.example.androidapp.provider

import android.content.ContentProvider
import android.net.Uri
import android.database.Cursor
import android.content.ContentValues
import android.os.ParcelFileDescriptor
import android.database.MatrixCursor
import com.github.kevinsawicki.http.HttpRequest
import org.json.simple.JSONValue
import org.json.simple.JSONObject
import java.util.ArrayList
import org.json.simple.JSONArray
import android.content.ContentUris

class LilliProvider : ContentProvider() {
    class object {
        val SCHEME = "http"
        val AUTHORITY = "lilli.etanzapinsky.com"
        val PACKAGE = "com.example.androidapp.provider"
        val COLUMNS = array("_ID", "authoritative_location", "_data")
    }

    public override fun onCreate(): Boolean {
        return true;
    }

    public override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val cursor = MatrixCursor(COLUMNS)
        val request = buildRequestFromUri(uri, "GET")

        if (request.ok()) {
            val response = JSONValue.parse(request.reader()) as? JSONObject
            cursor.addRow(response?.values())
        }

        return cursor;
    }

    fun buildRequestFromUri(uri: Uri?, method: String): HttpRequest {
        val url = Uri.Builder()
                     .scheme(SCHEME)
                    ?.authority(AUTHORITY)
                    ?.appendPath(uri?.getPath())
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

    fun buildUri(path: String?): Uri? {
        return Uri.parse(PACKAGE)
            ?.buildUpon()
            ?.scheme("content")
            ?.path(path)
            ?.build()
    }

    fun buildAttributeRequest(uri: Uri?, values: ContentValues?, method: String): HttpRequest {
        val request = buildRequestFromUri(uri, method)
        val stream = request.writer()
        val output = JSONObject()

        for (v in values?.valueSet()?.iterator()) {
            output.put(v.getKey(), v.getValue())
        }

        output.writeJSONString(stream)

        return request
    }

    public override fun getType(uri: Uri?): String? {
        val resource = uri?.getPathSegments()?.get(0) ?: "resource"
        return "vnd.android.cursor.item/vnd.com.example.androidapp.provider.${resource}"
    }

    public override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        val request = buildAttributeRequest(uri, values, "POST")

        if (request.created()) {
            val path = Uri.parse(request.location())?.getPath()
            return buildUri(path)
        } else {
            return null
        }
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