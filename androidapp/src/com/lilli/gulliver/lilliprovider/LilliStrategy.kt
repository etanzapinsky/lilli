package com.lilli.gulliver.lilliprovider

import org.json.JSONObject
import android.net.Uri
import android.content.Context

trait LilliStrategy {
    fun get(context : Context?, uri : Uri?, response : JSONObject?) : String?
}