package com.lilli.gulliver

import android.os.AsyncTask
import android.content.Context
import android.content.ContentValues
import android.widget.TextView

class AsyncDownloader(val downloader : Downloader?, val context : Context?, val options : Map<String, String?>?, val dbHelper : StatDbHelper?, val response : TextView?) : AsyncTask<String, Int, Pair<String?, Long?>?>() {
    var startTime : Long = 0

    protected override fun doInBackground(vararg p0 : String?) : Pair<String?, Long?>? {
        val first = p0.get(0)
        startTime = System.nanoTime()

        if (first != null) {
            val result = downloader?.getData(first, context, options)
            return Pair(first, result?.length())
        }

        return null
    }

    protected override fun onPostExecute(result : Pair<String?, Long?>?) {
        val message = if (result != null) {
            val elapsed = System.nanoTime() - startTime

            val values = ContentValues()
            values.put(StatContract.StatEntry.COLUMN_NAME_NAME, result.first)
            values.put(StatContract.StatEntry.COLUMN_NAME_SIZE, result.second)
            values.put(StatContract.StatEntry.COLUMN_NAME_TIME, elapsed)
            values.put(StatContract.StatEntry.COLUMN_NAME_METHOD, downloader.toString())

            val db = dbHelper?.getWritableDatabase()

            if (db != null) {
                db.insert(StatContract.StatEntry.TABLE_NAME, null, values)
            }

            "Elapsed: ${elapsed / 1000000} milliseconds\nDB: ${db?.getPath()}"
        } else {
            "Please enter a valid URL"
        }

        response?.setText(message)
    }
}