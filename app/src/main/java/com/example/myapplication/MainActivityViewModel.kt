package com.example.myapplication

import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.example.myapplication.connection.Device
import com.example.myapplication.connection.WifiBroadcastReceiver

class MainActivityViewModel : ViewModel() {
    private val receiver = WifiBroadcastReceiver(MainApplication.appContext)
    private lateinit var device: Device
    fun connect(wifiP2pDevice: WifiP2pDevice) {
        receiver.connect(wifiP2pDevice)
    }

    fun startScan() {
        receiver.startScan()
    }

    fun sendMessage(message: String) {
        device = receiver.getDevice()
        device.sendMessage(message)
    }

    fun receiveFile() {
        device = receiver.getDevice()
        device.receiveFile()
    }

    fun sendFile(uri: Uri) {
        device.sendFile(uri)
    }

    @Composable
    fun getDevices(): WifiP2pDeviceList? = Emiters.getDevices()

    @Composable
    fun isUploadStarted(): Boolean = Emiters.isUploadStarted()

    @Composable
    fun connectionState(): Boolean = Emiters.connectionState()


}