package com.lilli.gulliver.lilliprovider

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.net.Socket
import org.apache.commons.io.IOUtils
import android.content.Intent
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.os.Messenger
import android.os.Message
import android.os.Bundle
import android.util.Log
import android.os.Handler

class WifiDirectStrategy {
    class object : LilliStrategy {
        private val AUTHORITY  = "WifiDirect"

        /** Messenger for communicating with the service. */
        private var mService : Messenger? = null

        /** Flag indicating whether we have called bind on the service. */
        private var mBound : Boolean = false

        /**
         * Class for interacting with the main interface of the service.
         */
        private val mConnection = object : ServiceConnection {
            public override fun onServiceConnected(className : ComponentName?, service: IBinder?) {
                // This is called when the connection with the service has been
                // established, giving us the object we can use to
                // interact with the service.  We are communicating with the
                // service using a Messenger, so here we get a client-side
                // representation of that from the raw IBinder object.
                mService = Messenger(service)
                mBound = true
            }

            public override fun onServiceDisconnected(className : ComponentName?) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mService = null
                mBound = false
            }
        }

        public fun connect(context : Context?, uri : Uri?) {
            context?.bindService(Intent(context, javaClass<WifiDirectService>()), mConnection, Context.BIND_AUTO_CREATE)
            val key = uri?.getQueryParameter(LilliContract.USERNAME)

            if (mBound) {
                val data = Bundle()
                data.putString(LilliContract.USERNAME, key)
                // Create and send a message to the service, using a supported 'what' value
                val msg = Message.obtain(null, WifiDirectService.REQUEST_RESOURCE, 0, 0)
                msg?.setData(data)
                mService?.send(msg)
            }

        }

        override fun get(context: Context?, uri: Uri?, response: JSONObject?): String? {
            return NetworkStrategy.get(context, uri, response)
        }

        public fun disconnect(context : Context?) {
            context?.unbindService(mConnection)
        }
    }
}
