package com.lilli.gulliver.lilliprovider

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.content.Context
import android.content.BroadcastReceiver
import android.os.Messenger
import android.os.Handler
import android.os.Message
import android.os.RemoteException
import android.app.IntentService
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceInfo
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceRequest
import android.net.wifi.p2p.WifiP2pManager.UpnpServiceResponseListener
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.WpsInfo

class WiFiDirectService : IntentService("WiFiDirectService") {
    private var mManager : WifiP2pManager? = null;
    private var mChannel : Channel? = null
    private val intentFilter = IntentFilter()

    protected override fun onHandleIntent(intent: Intent?) {

        val serviceRequest = WifiP2pUpnpServiceRequest.newInstance("uuid:sdasda")

        mManager?.addServiceRequest(mChannel,
                serviceRequest,
                object : ActionListener {
                    override public fun onSuccess() {}

                    override public fun onFailure(arg0 : Int) {}
                })

        mManager?.discoverServices(mChannel, object : ActionListener {
            override public fun onSuccess() {}

            override public fun onFailure(arg0 : Int) {}
        })

        val listener = object : UpnpServiceResponseListener {
            public override fun onUpnpServiceAvailable(uniqueServiceNames: List<String>?, srcDevice: WifiP2pDevice?) {
                val config = WifiP2pConfig()
                config.deviceAddress = srcDevice?.deviceAddress

                mManager?.connect(mChannel, config, object : ActionListener {
                    override public fun onSuccess() {
                        val ip = srcDevice?.deviceAddress

                    }

                    override public fun onFailure(arg0 : Int) {}
                })

            }
        }

        mManager?.setUpnpServiceResponseListener(mChannel, listener)
    }

    private fun startRegistration() {
        val serviceInfo =  WifiP2pUpnpServiceInfo.newInstance("sdasda",
                "urn:schemas-upnp-org:device:Oracle:1",
                listOf("urn:schemas-upnp-org:service:Lilli:1"))

        mManager?.addLocalService(mChannel, serviceInfo, object : ActionListener {
            override public fun onSuccess() {}

            override public fun onFailure(arg0 : Int) {}
        })
    }

    public override fun onCreate() {
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        mChannel = mManager?.initialize(this, getMainLooper(), null)

        startRegistration()
    }
}
