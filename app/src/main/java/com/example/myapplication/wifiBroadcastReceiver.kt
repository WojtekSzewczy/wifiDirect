package com.example.myapplication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SuppressLint("MissingPermission")
class WifiBroadcastReceiver(private val context: Context) : BroadcastReceiver() {

    private val manager: WifiP2pManager by lazy(LazyThreadSafetyMode.NONE) {
        context.applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val channel: WifiP2pManager.Channel =
        manager.initialize(context, Looper.getMainLooper(), null)

    private val discoveryListener = object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
        }

        override fun onFailure(error: Int) {
        }
    }
    private val config = WifiP2pConfig()
    lateinit var client :Client
    lateinit var server :FileServerAsyncTask
    private lateinit var deviceType :DeviceType
    private val mainScope = MainScope()

    private val _devices = MutableSharedFlow<WifiP2pDeviceList>()
    private val _connectionState = MutableSharedFlow<Boolean>()
    val devices: Flow<WifiP2pDeviceList>
        get() = _devices
    val connectionState : Flow<Boolean>
        get() = _connectionState

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val TAG = WifiBroadcastReceiver::class.simpleName

    init {
        Log.v(TAG , "init")
        context.applicationContext.registerReceiver(this, intentFilter)
        manager.requestConnectionInfo(channel){
            if(it.groupFormed){
                manager.removeGroup(channel,object : WifiP2pManager.ActionListener{
                    override fun onSuccess() {
                        Log.v(TAG, "Succesfully removed group")
                    }

                    override fun onFailure(p0: Int) {
                        Log.v(TAG, "failed to remove group removed")

                    }

                })
            }
        }
        manager.discoverPeers(channel, discoveryListener)

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                Log.v(TAG,"WIFI_P2P_STATE_CHANGED_ACTION")
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.v(TAG,"WIFI_P2P_PEERS_CHANGED_ACTION")
                manager.requestPeers(channel) { peers: WifiP2pDeviceList ->
                    mainScope.launch {_devices.emit(peers)
                        peers.deviceList.forEach {
                            Log.v("P2P receiver", it.deviceName + " " + it.deviceAddress)
                        }
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.v(TAG,"WIFI_P2P_CONNECTION_CHANGED_ACTION")
                                                    // to sie dzieje przy połączeniu
                manager.requestConnectionInfo(channel // na obu urządzeniach z zaistalowana aplikacja jest to wołane
                ) { wifiP2pInfo -> // wiec urządzenie wie czy jest hostem czy adminem w zależności czy spełnia te warunki
                    MainScope().launch { _connectionState.emit(wifiP2pInfo?.groupFormed == true) }

                    if (wifiP2pInfo?.groupFormed == true && wifiP2pInfo.isGroupOwner) {
                        Toast.makeText(this.context, "I'm host", Toast.LENGTH_SHORT).show()
                        Log.v(TAG, "i is host")
                        server = FileServerAsyncTask(this.context)

                        deviceType = DeviceType.SERVER
                        //mainScope.launch{Log.v(DEFAULT_TAG,server.deferred.await())}

                    } else if (wifiP2pInfo?.groupFormed == true) {
                        Toast.makeText(this.context, "I'm client", Toast.LENGTH_SHORT).show()
                        Log.v(TAG, "i is client")
                        client= Client(this.context, wifiP2pInfo.groupOwnerAddress)

                        deviceType = DeviceType.CLIENT
                    }
                }

            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.v(TAG,"WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
            }
        }
    }

    fun sendMessage(message : String){
        when(deviceType){
            DeviceType.SERVER -> {
                server.sendMessage(message)
            }
            DeviceType.CLIENT -> {
                client.sendMessage(message)
            }
        }
    }
    fun readMessage():String{
        return when(deviceType){
            DeviceType.SERVER -> {
                runBlocking{server.readMessage().await()}
            }

            DeviceType.CLIENT -> {
               runBlocking {client.readMessage().await()}
            }
        }
    }

    fun connect(wifiP2pDevice: WifiP2pDevice) {
        config.deviceAddress = wifiP2pDevice.deviceAddress
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
    private enum class DeviceType{
        CLIENT, SERVER
    }
}