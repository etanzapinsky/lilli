package com.lilli.gulliver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.location.Location
import com.lilli.gulliver.lilliprovider.LilliContract
import com.lilli.gulliver.lilliprovider.LilliProvider
import android.content.ContentValues

class LocationReceiver : BroadcastReceiver() {
    public override fun onReceive(context: Context?, intent: Intent?) {
        val resolver = context?.getContentResolver()
        val location = intent?.getExtras()?.get(LocationManager.KEY_LOCATION_CHANGED) as? Location
        val latitude = location?.getLatitude()
        val longitude = location?.getLongitude()

        val uri = LilliContract.Edges.CONTENT_URI
                 .buildUpon()
                ?.appendQueryParameter(LilliContract.USERNAME, LilliDownloader.USERNAME)
                ?.appendQueryParameter(LilliContract.PASSWORD, LilliDownloader.PASSWORD)
                ?.build()

        val values = ContentValues()
        values.put(LilliContract.Edges.LOCATION, "%s,%s".format(latitude, longitude))

//        resolver?.update(uri, values, null, null)
    }
}