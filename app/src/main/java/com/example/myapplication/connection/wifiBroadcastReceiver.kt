package com.example.myapplication.connection

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import com.example.myapplication.Emiters
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class WifiBroadcastReceiver(private val context: Context) : BroadcastReceiver() {

    private val manager: WifiP2pManager by lazy(LazyThreadSafetyMode.NONE) {
        context.applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val channel: WifiP2pManager.Channel =
        manager.initialize(context, Looper.getMainLooper(), null)

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val TAG = WifiBroadcastReceiver::class.simpleName
    private val mainScope = MainScope()
    private lateinit var device: Device
    var flag = false


    init {
        context.applicationContext.registerReceiver(this, intentFilter)
        manager.requestConnectionInfo(channel) {

            manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {

                }

                override fun onFailure(p0: Int) {
                }
            })

        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                Log.v(TAG, "WIFI_P2P_STATE_CHANGED_ACTION")
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.v(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")

                getPeers()
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.v(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION")
                manager.requestConnectionInfo(channel) {
                    if (it.groupFormed) {
                        onConnected()
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.v(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
            }
        }
    }

    fun startScan() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
            }

            override fun onFailure(error: Int) {
            }
        })
    }

    private fun getPeers() {
        Log.v(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
        manager.requestPeers(channel) { peers: WifiP2pDeviceList ->
            mainScope.launch {
                Emiters.emitDevices(peers)
                peers.deviceList.forEach {
                    Log.v("P2P receiver", it.deviceName + " " + it.deviceAddress)
                }
            }
        }
    }

    fun connect(wifiP2pDevice: WifiP2pDevice) {
        val config = WifiP2pConfig().apply { deviceAddress = wifiP2pDevice.deviceAddress }
        channel.also { channel ->
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.v(TAG, "Success")
                }

                override fun onFailure(p0: Int) {
                    Log.v(TAG, "Fail")
                }

            })
        }
    }

    private fun onConnected() {
        manager.requestConnectionInfo(channel) { wifiP2pInfo ->
            MainScope().launch { Emiters.emitConnectionState(wifiP2pInfo.groupFormed) } // została stworzona grupa - czy nawiązano połączenie
            device = Device(wifiP2pInfo, context)
        }
    }

    fun getDevice() = device

}