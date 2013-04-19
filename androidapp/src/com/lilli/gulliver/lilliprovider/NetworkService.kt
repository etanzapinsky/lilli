package com.lilli.gulliver.lilliprovider

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.net.ServerSocket
import java.net.Socket

class NetworkService : Service() {
    public override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread(LilliNetworkServer()).start()
        return Service.START_STICKY
    }
}

class LilliNetworkWorker(val socket : Socket) : Runnable {
    private val BUFFER_SIZE = 4096

    public override fun run() {
        while (true) {
            val request = read()
            send(request)
        }
    }

    private fun send(payload : String) {
        val outStream = socket.getOutputStream()
        outStream?.write(payload.getBytes())
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

class LilliNetworkServer : Runnable {
    private val PORT = 4119
    private val receiverServerSocket = ServerSocket(PORT)

    public override fun run() {
        receiverServerSocket.setReuseAddress(true)
        while (true) {
            val receiverSocket = receiverServerSocket.accept()
            Thread(LilliNetworkWorker(receiverSocket)).start()
        }
    }

}
