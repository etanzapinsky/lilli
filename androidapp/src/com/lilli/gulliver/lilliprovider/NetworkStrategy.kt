package com.lilli.gulliver.lilliprovider

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.net.Socket
import org.apache.commons.io.IOUtils
import android.util.Log

class NetworkStrategy {
    class object : LilliStrategy {
        override fun get(context: Context?, uri: Uri?, response: JSONObject?): String? {
            Log.d("NetworkStrategy", "DOING IT WITH ${response?.getString("ip")}")
            val socket = Socket(response?.getString("ip"), LilliNetworkServer.PORT)
            val out = socket.getOutputStream()

            val key = uri?.getLastPathSegment()?.getBytes()

            if (key != null) {
                out?.write(key)
                out?.flush()
            }

            if (socket.isClosed()) {
                Log.d("NetworkStrategy", "LO SIENTO")
                return null
            }

            val filename = LilliProvider.getFilename(uri)
            val fos = context?.openFileOutput(filename, Context.MODE_PRIVATE)
            val input = socket.getInputStream()

            IOUtils.copy(input, fos)
            fos?.close()

            return context?.getFileStreamPath(filename)?.getPath()
        }
    }
}
