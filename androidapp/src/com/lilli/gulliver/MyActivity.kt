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
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.AdapterView
import android.widget.Adapter
import com.lilli.gulliver.lilliprovider.NetworkService
import com.lilli.gulliver.lilliprovider.WifiDirectService
import android.util.Log
import com.lilli.gulliver.lilliprovider.LilliContract

class MyActivity() : Activity() {
    class object {
        private val LOCATION_ACTION = "com.lilli.gulliver.LOCATION_UPDATE_RECEIVED"
    }

    private var responseMessage : TextView? = null
    private var mDbHelper : StatDbHelper? = null
    private var currentlySelectedProvider : String? = null
    private var currentlySelectedAlgorithm : String? = null
    private var algorithm_spinner : Spinner? = null
    private val downloaders = mapOf(
            "Origin" to OriginDownloader,
            "CDN" to CDNDownloader,
            "Lilli" to LilliDownloader,
            "BitTorrent" to TorrentDownloader
    )
    private val resources = mapOf(
            "Origin" to array("http://lilli.etanzapinsky.com/resource1.jpg",
                              "http://lilli.etanzapinsky.com/resource2.jpg",
                              "http://lilli.etanzapinsky.com/resource3.jpg"),
            "CDN" to array("http://uploads.samaarons.com/resource1.jpg",
                           "http://uploads.samaarons.com/resource2.jpg",
                           "http://uploads.samaarons.com/resource3.jpg"),
            "Lilli" to array("resource1", "resource2", "resource3"),
            "BitTorrent" to array("/sdcard/resource1.jpg.torrent",
                                  "/sdcard/resource2.jpg.torrent",
                                  "/sdcard/resource3.jpg.torrent")
    )

    protected override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupDB()
        startLocationUpdates()
        startNetworkService()
        startWifiService()
    }

    private fun setupView() {
        setContentView(R.layout.main)
        responseMessage = findViewById(R.id.response_text) as? TextView

        setupProviderSpinner()
        setupAlgorithmSpinner()
    }

    private fun setupDB() {
        mDbHelper = StatDbHelper(this)
    }

    private fun setupAlgorithmSpinner() {
        algorithm_spinner = findViewById(R.id.lilli_spinner) as? Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.lilli_spinner_options, android.R.layout.simple_spinner_item)
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        algorithm_spinner?.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<out Adapter?>?) {
                return
            }

            override fun onItemSelected(parent: AdapterView<out Adapter?>?, view: View?, position: Int, id: Long) {
                currentlySelectedAlgorithm = parent?.getItemAtPosition(position).toString()
            }
        })
        algorithm_spinner?.setAdapter(adapter)
    }

    private fun setupProviderSpinner() {
        val spinner = findViewById(R.id.spinner) as? Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.dropdown_options, android.R.layout.simple_spinner_item)
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<out Adapter?>?) {
                return
            }

            override fun onItemSelected(parent: AdapterView<out Adapter?>?, view: View?, position: Int, id: Long) {
                currentlySelectedProvider = parent?.getItemAtPosition(position).toString()
                when (currentlySelectedProvider) {
                    "Lilli" -> algorithm_spinner?.setVisibility(View.VISIBLE)
                    else -> algorithm_spinner?.setVisibility(View.GONE)
                }
            }
        })
        spinner?.setAdapter(adapter)
    }

    private fun startLocationUpdates() {
        val intent = Intent(LOCATION_ACTION)
        val pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val lm = getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        lm?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0.0, pi)
    }

    private fun startNetworkService() {
        val serviceIntent = Intent(this, javaClass<NetworkService>())
        startService(serviceIntent)
    }

    private fun startWifiService() {
        val wifiDirectIntent = Intent(this, javaClass<WifiDirectService>())
        startService(wifiDirectIntent)
    }

    public override fun onCreateOptionsMenu(menu : Menu?): Boolean {
        val inflater = getMenuInflater()
        inflater?.inflate(R.menu.main_activity, menu)
        return true
    }

    protected override fun onStop() {
        super.onStop()
        backupDb()
    }

    public fun requestURL(view : View) {
        val editText = findViewById(R.id.url_string) as? EditText
        val message = editText?.getText().toString()
        getResource(message)
    }

    public fun getResource(resource : String) {
        val downloader = downloaders[currentlySelectedProvider]
        val options = hashMapOf<String, String?>()

        if (currentlySelectedProvider == "Lilli") {
            options.put(LilliContract.ALGORITHM, currentlySelectedAlgorithm)
        }

        val connMgr = getApplicationContext()?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connMgr?.getActiveNetworkInfo()
        if (networkInfo != null && networkInfo.isConnected()) {
            AsyncDownloader(downloader, this, options, mDbHelper, responseMessage).execute(resource)
        }
    }

    public fun getBuiltinResource(view : View) {
        val index = when (view.getId()) {
            R.id.button1 -> 0
            R.id.button2 -> 1
            R.id.button3 -> 2
            else -> null
        }

        if (index != null) {
            getResource(resources[currentlySelectedProvider]?.get(index))
        }
    }

    public fun deleteDb(item : MenuItem?) {
        TrashDialogFragment().show(getFragmentManager(), "trash")
    }

    public fun refreshLocation(item : MenuItem?) {
        val intent = Intent(LOCATION_ACTION)
        val lm = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
        sendBroadcast(intent)
    }

    public fun backupDb() {
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