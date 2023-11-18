package com.example.myapplication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

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
    private val mainScope = MainScope()
    private val config = WifiP2pConfig()
    private val _devices = MutableSharedFlow<WifiP2pDeviceList>()
    private val _connectionState = MutableSharedFlow<Boolean>()
    private val _uploadStart = MutableSharedFlow<Boolean>()
    val devices: Flow<WifiP2pDeviceList>
        get() = _devices
    val connectionState: Flow<Boolean>
        get() = _connectionState
    val uploadStart: Flow<Boolean>
        get() = _uploadStart

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val TAG = WifiBroadcastReceiver::class.simpleName

    private lateinit var messagingMenager: MessagingMenager
    private lateinit var uri: Uri
    private lateinit var wifiP2Pinfo: WifiP2pInfo
    private lateinit var uploadListener: UploadListener
    private lateinit var device: Device


    init {
        context.applicationContext.registerReceiver(this, intentFilter)
        manager.requestConnectionInfo(channel) {
            if (it.groupFormed) {
                manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
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
                getPeers()
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                onConnected()
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.v(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
            }
        }
    }
    private fun getPeers() {
        Log.v(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
        manager.requestPeers(channel) { peers: WifiP2pDeviceList ->
            mainScope.launch {
                _devices.emit(peers)
                peers.deviceList.forEach {
                    Log.v("P2P receiver", it.deviceName + " " + it.deviceAddress)
                }
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

    private fun onConnected() {
        Log.v(
            TAG,
            "WIFI_P2P_CONNECTION_CHANGED_ACTION"
        )

        manager.requestConnectionInfo(channel) { obtainedWifiP2pInfo ->
            MainScope().launch { _connectionState.emit(obtainedWifiP2pInfo.groupFormed) } // została stworzona grupa - czy nawiązano połączenie
            device = Device(obtainedWifiP2pInfo)

            setDeviceRole() // w zależności od tego czy urzadzenie jest hostem czy klientem tworzy dopowiednia klase
        }
    }

    private fun setDeviceRole() = if (wifiP2Pinfo.isGroupOwner) createServer() else createClient()

    private fun createServer() {
        Toast.makeText(this.context, "I'm host", Toast.LENGTH_SHORT).show()
        val server = Server(this.context)
        enableComunication(server)
    }

    private fun createClient() {
        Toast.makeText(this.context, "I'm client", Toast.LENGTH_SHORT).show()
        val client = Client(this.context, wifiP2Pinfo.groupOwnerAddress)
        enableComunication(client)
    }

    private fun enableComunication(connectable: MessagingInterface) {
        messagingMenager = MessagingMenager(connectable)
        uploadListener = UploadListener(connectable)
        mainScope.launch(Dispatchers.IO) { _uploadStart.emit(uploadListener.listenForUpload()) }
    }

    fun sendMessage(message: String) {
        messagingMenager.sendMessage(message)
    }

    fun sendFile(uri: Uri) {
        messagingMenager.sendFile(uri)
    }

    fun receiveFile() {
        messagingMenager.receiveFile()
    }


    fun getUri(receivedUri: Uri) {
        Log.v(TAG, receivedUri.path!!)
        uri = receivedUri
    }

}