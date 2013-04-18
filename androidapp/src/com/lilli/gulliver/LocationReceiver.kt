package com.lilli.gulliver

import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver

class LocationReceiver : BroadcastReceiver() {
    public override fun onReceive(context: Context?, intent: Intent?) {
        Thread(AsyncLocationReceiver(context, intent, goAsync())).start()
    }
}