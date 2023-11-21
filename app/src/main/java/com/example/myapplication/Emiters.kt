package com.example.myapplication

import android.net.wifi.p2p.WifiP2pDeviceList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object Emiters {
    private val mainScope = MainScope()
    private val _uploadStart = MutableSharedFlow<Boolean>()
    val uploadStart: Flow<Boolean>
        get() = _uploadStart

    private val _connectionState = MutableSharedFlow<Boolean>()
    val connectionState: Flow<Boolean>
        get() = _connectionState

    private val _devices = MutableSharedFlow<WifiP2pDeviceList>()
    val devices: Flow<WifiP2pDeviceList>
        get() = _devices

    fun emitUploadStarted(state: Boolean) {
        mainScope.launch { _uploadStart.emit(state) }
    }

    fun emitConnectionState(state: Boolean) {
        mainScope.launch { _connectionState.emit(state) }
    }

    fun emitDevices(wifiP2pDeviceList: WifiP2pDeviceList) {
        mainScope.launch { _devices.emit(wifiP2pDeviceList) }
    }

    @Composable
    fun getDevices() = devices.collectAsState(initial = null).value

    @Composable
    fun connectionState() = connectionState.collectAsState(initial = false).value

    @Composable
    fun isUploadStarted() = uploadStart.collectAsState(initial = false).value


}