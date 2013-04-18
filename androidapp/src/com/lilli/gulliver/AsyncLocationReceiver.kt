package com.lilli.gulliver

import android.content.BroadcastReceiver.PendingResult
import android.content.Intent
import android.content.Context
import android.location.LocationManager
import android.location.Location
import com.lilli.gulliver.lilliprovider.LilliContract
import android.util.Log
import android.content.ContentValues

class AsyncLocationReceiver(val context : Context?, val intent : Intent?, val pending : PendingResult?) : Runnable {
    public override fun run() {
        val resolver = context?.getContentResolver()
        val location = intent?.getExtras()?.get(LocationManager.KEY_LOCATION_CHANGED) as? Location
        val latitude = location?.getLatitude()
        val longitude = location?.getLongitude()

        val credentials = LilliDownloader.getCredentials(context)
        val username = credentials.get(LilliContract.USERNAME)
        val password = credentials.get(LilliContract.PASSWORD)

        val uri = LilliContract.Edges.CONTENT_URI
                 .buildUpon()
                ?.appendPath(username)
                ?.appendQueryParameter(LilliContract.USERNAME, username)
                ?.appendQueryParameter(LilliContract.PASSWORD, password)
                ?.build()

        val values = ContentValues()
        values.put(LilliContract.Edges.LOCATION, "POINT(%s %s)".format(longitude, latitude))

        resolver?.update(uri, values, null, null)

        pending?.finish()
    }
}