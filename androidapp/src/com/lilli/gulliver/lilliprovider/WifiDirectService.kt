package com.lilli.gulliver.lilliprovider

import android.os.AsyncTask
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.app.Activity
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.util.Log
import java.util.ArrayList
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.WpsInfo
import android.net.NetworkInfo
import android.app.Service
import android.os.IBinder
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo
import android.os.Binder
import java.io.File
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceInfo
import com.lilli.gulliver.LilliDownloader
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceRequest
import android.os.StrictMode

class WifiDirectService : Service() {
    private val SERVICE = "urn:schemas-upnp-org:serivice:lilli:1"
    private val AUTHORITY = "WifiDirect"
    private val intentFilter = IntentFilter()
    private var receiver : WifiDirectBroadcastReceiver? = null
    private var mManager : WifiP2pManager? = null
    private var mChannel : Channel? = null

    private val mBinder = object : Binder() {
        public fun getFile(peerPublicKey : String, resource : String) : File? {
            // perform WifiDirect connection and receiveing of data here.
//            val device = peers.get(0)
//            val config = WifiP2pConfig()
//            config.deviceAddress = device.deviceAddress
//            config.wps?.setup = WpsInfo.PBC
//            mManager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {}
//                override fun onFailure(reason : Int) {Log.d(AUTHORITY, "connection to peer failed")}
//            })
            return null
        }
    }

    public override fun onCreate() {
        //  Indicates a change in the Wi-Fi Peer-to-Peer status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        mManager = this?.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        mChannel = mManager?.initialize(this, this.getMainLooper(), null)

        receiver = WifiDirectBroadcastReceiver(mManager, mChannel)
        this.registerReceiver(receiver, intentFilter)
    }

    public override fun onStartCommand(intent : Intent?, start : Int, flags : Int) : Int {
        val oldThreadPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        val creds = LilliDownloader.getCredentials(this)
        StrictMode.setThreadPolicy(oldThreadPolicy)
        mManager?.addLocalService(mChannel, WifiP2pUpnpServiceInfo.newInstance(creds.get(LilliContract.USERNAME), "", listOf(SERVICE)),
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {Log.d(AUTHORITY, "Added lilli service success")}
                    override fun onFailure(reasonCode : Int) {Log.d(AUTHORITY, "Added lilli service failure")}
                })

        mManager?.addServiceRequest(mChannel, WifiP2pUpnpServiceRequest.newInstance(SERVICE),
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {Log.d(AUTHORITY, "Added lilli service success")}
                    override fun onFailure(reasonCode : Int) {Log.d(AUTHORITY, "Added lilli service failure")}
                })

        mManager?.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {Log.d(AUTHORITY, "peer discovery initialization success")}
            override fun onFailure(reasonCode : Int) {Log.d(AUTHORITY, "peer discovery initialization failure")}
        })
        return 0.toInt()
    }

    public override fun onBind(intent : Intent?) : IBinder? {
        return mBinder
    }

    public override fun onDestroy() {
        this.unregisterReceiver(receiver);
    }


}

class WifiDirectBroadcastReceiver(val mManager : WifiP2pManager?, val mChannel : Channel?) : BroadcastReceiver() {
    private val AUTHORITY = "WifiDirect"

    private val peers = ArrayList<WifiP2pDevice>()

    private val peerListListener = object : PeerListListener {
        override fun onPeersAvailable(peerList : WifiP2pDeviceList?) {
            peers.clear()
            val newPeers = peerList?.getDeviceList()
            if (newPeers != null) {
                peers.addAll(newPeers)
            }
            for(p in peers) {
                Log.d(AUTHORITY, p.toString())
            }
        }
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getAction()
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi Direct mode is enabled or not, alert
            // the Activity.
            val state = intent?.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(AUTHORITY, "p2p enabled")
            } else {
                Log.d(AUTHORITY, "p2p not enabled")
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed!  We should probably do something about
            // that.
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
            if (mManager == null) {
                return;
            }
            val networkInfo : NetworkInfo? = intent?.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)

            if (networkInfo != null && networkInfo.isConnected()) {
                //                    mManager?.requestConnectionInfo(mChannel)
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}
