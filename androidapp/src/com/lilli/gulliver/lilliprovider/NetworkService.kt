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

class NetworkService : Service() {
    public override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread(LilliNetworkServer(getApplicationContext())).start()
        return Service.START_STICKY
    }
}

class LilliNetworkWorker(val context : Context?, val socket : Socket) : Runnable {
    private val BUFFER_SIZE = 4096

    public override fun run() {
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
    private val PORT = 4119
    private val receiverServerSocket = ServerSocket(PORT)

    public override fun run() {
        receiverServerSocket.setReuseAddress(true)
        while (true) {
            val receiverSocket = receiverServerSocket.accept()
            Thread(LilliNetworkWorker(context, receiverSocket)).start()
        }
    }

}
