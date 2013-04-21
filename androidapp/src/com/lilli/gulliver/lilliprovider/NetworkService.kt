package com.lilli.gulliver.lilliprovider

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.net.ServerSocket
import java.net.Socket
import android.content.Context
import java.io.File
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import android.util.Log
import java.net.InetSocketAddress

class NetworkService : Service() {
    public override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    public override fun onCreate() {
        Thread(LilliNetworkServer(getApplicationContext())).start()
    }
}

class LilliNetworkWorker(val context : Context?, val socket : Socket) : Runnable {
    private val BUFFER_SIZE = 4096

    public override fun run() {
        Log.d("LilliNetworkWorker", "INCOMING NETWORK REQUEST!!!")
        val request = read()
        val uri = LilliContract.Objects.CONTENT_URI
                .buildUpon()
               ?.appendPath(request.trim())
               ?.build()

        val resolver = context?.getContentResolver()
        val cursor = resolver?.query(uri, array(LilliContract.Objects.CACHED_DATA), null, null, null)

        cursor?.moveToFirst()

        val path = cursor?.getString(0)

        if (path != null) {
            Log.d("LilliNetworkWorker", "SENDING FILE OVER NETWORK!")
            IOUtils.copy(FileInputStream(File(path)), socket.getOutputStream())
        }

        socket.close()
    }

    private fun read() : String {
        val buffer = ByteArray(BUFFER_SIZE)
        var msgSize = 0
        val inStream = socket.getInputStream()
        while (true) {
            msgSize = inStream?.read(buffer) ?: 0
            while (msgSize > 0) {
                return String(buffer, 0, msgSize)
            }
        }
    }
}

class LilliNetworkServer(val context : Context?) : Runnable {
    class object {
        val PORT = 6998
    }
    private val receiverServerSocket = ServerSocket()

    public override fun run() {
        receiverServerSocket.setReuseAddress(true)
        receiverServerSocket.bind(InetSocketAddress(PORT))
        while (true) {
            val receiverSocket = receiverServerSocket.accept()
            Thread(LilliNetworkWorker(context, receiverSocket)).start()
        }
    }

}
