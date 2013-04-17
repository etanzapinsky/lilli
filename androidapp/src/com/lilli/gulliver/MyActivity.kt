/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 3/19/13
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */

package com.lilli.gulliver

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import java.io.File
import android.os.Environment
import java.io.FileInputStream
import java.io.FileOutputStream
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.app.PendingIntent
import android.content.Intent
import android.location.LocationManager

class MyActivity() : Activity() {
    class object {
        private val LOCATION_ACTION = "com.lilli.gulliver.LOCATION_UPDATE_RECEIVED"
    }

    private var responseMessage : TextView? = null
    private var mDbHelper : StatDbHelper? = null
    private val spinnerActivity = SpinnerActivity()

    protected override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val spinner = findViewById(R.id.spinner) as? Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.dropdown_options, android.R.layout.simple_spinner_item)
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.setOnItemSelectedListener(spinnerActivity)
        spinner?.setAdapter(adapter)

        responseMessage = findViewById(R.id.response_text) as? TextView
        mDbHelper = StatDbHelper(this)

        val intent = Intent(LOCATION_ACTION)
        val pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val lm = getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        lm?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0.0, pi)
    }

    protected override fun onStop() {
        super.onStop()
        backupDb()
    }

    public fun requestURL(view : View) {
        val editText = findViewById(R.id.url_string) as? EditText
        val message = editText?.getText().toString()

        val downloader = when (spinnerActivity.currentlySelected) {
            "Origin" -> OriginDownloader()
            "Lilli" -> LilliDownloader()
            "BitTorrent" -> TorrentDownloader()
            else -> null
        }

        val connMgr = getApplicationContext()?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connMgr?.getActiveNetworkInfo()
        if (networkInfo != null && networkInfo.isConnected()) {
            AsyncDownloader(downloader, this, mDbHelper, responseMessage).execute(message)
        }
    }

    public fun deleteDb(view : View) {
        backupDb()
        this.deleteDatabase(StatDbHelper.DATABASE_NAME)
    }

    protected fun backupDb() {
        try {
            val sd = Environment.getExternalStorageDirectory()

            if (sd!!.canWrite()) {
                val currentDBPath = mDbHelper?.getWritableDatabase()?.getPath()
                val backupDBPath = "lilli.db"
                if (currentDBPath != null) {
                    val currentDB = File(currentDBPath)
                    val backupDB = File(sd, backupDBPath)

                    if (currentDB.exists()) {
                        val src = FileInputStream(currentDB).getChannel()
                        val dst = FileOutputStream(backupDB).getChannel()
                        dst.transferFrom(src, 0, src?.size())
                        src?.close()
                        dst.close()
                    }
                }
            }
        }
        catch (e : Exception) {}
    }

}