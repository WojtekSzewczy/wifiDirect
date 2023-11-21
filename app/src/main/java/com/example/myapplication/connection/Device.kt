package com.example.myapplication.connection

import android.content.Context
import android.net.Uri
import android.net.wifi.p2p.WifiP2pInfo
import android.util.Log
import android.widget.Toast
import com.example.myapplication.Emiters
import com.example.myapplication.messaging.MessagingInterface
import com.example.myapplication.messaging.MessagingMenager
import com.example.myapplication.messaging.client_server.Client
import com.example.myapplication.messaging.client_server.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Device(private val wifiP2Pinfo: WifiP2pInfo, private val context: Context) {

    private lateinit var messagingMenager: MessagingMenager
    private lateinit var uploadListener: UploadListener
    private val mainScope = MainScope()

    init {
        setDeviceRole()
    }

    private fun setDeviceRole() = if (wifiP2Pinfo.isGroupOwner) createServer() else createClient()

    private fun createServer() {
        Toast.makeText(context, "I'm host", Toast.LENGTH_SHORT).show()
        val server = Server(context)
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
        mainScope.launch(Dispatchers.IO) {
            Log.v("Device", "launch")
            delay(1000L)
            Log.v("Device", "launch after delay")
            Emiters.emitUploadStarted(uploadListener.listenForUpload())
        }
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


}