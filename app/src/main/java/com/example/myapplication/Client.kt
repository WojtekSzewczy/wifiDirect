package com.example.myapplication

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class Client(private val context: Context, hostAddress: InetAddress) : Connectable(),
    MessagingInterface {
    private val serverPort = 8888
    private val clientSocket = Socket()
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream


    init {
        try {
            MainScope().launch(Dispatchers.IO) {
                initClient(hostAddress)
            }
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        } finally {
            clientSocket.takeIf { it.isConnected }?.apply {
                close()
            }

        }
    }

    private suspend fun initClient(hostAddress: InetAddress) {
        delay(500L)
        clientSocket.bind(null)
        clientSocket.connect((InetSocketAddress(hostAddress, serverPort)))
        inputStream = clientSocket.getInputStream()
        outputStream = clientSocket.getOutputStream()
    }

    override fun sendFile(uri: Uri) {
        super.sendFile(uri, outputStream, context)
    }

    override fun receiveFile() {
        super.receiveFile(inputStream, context)
    }

    override fun readMessage(): Deferred<String> = readMessage(inputStream)
    override fun sendMessage(message: String) = sendMessage(message, outputStream)
}