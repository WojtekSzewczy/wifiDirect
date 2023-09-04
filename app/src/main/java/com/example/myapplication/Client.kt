package com.example.myapplication

import android.content.Context
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class Client(private val carContext: Context, hostAddress: InetAddress) {
    private val serverPort = 8888
    private val clientSocket = Socket()
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream : InputStream

    init {
        MainScope().launch(Dispatchers.IO) {
            delay(500L)
            clientSocket.bind(null)
            clientSocket.connect((InetSocketAddress(hostAddress, serverPort)))
            inputStream = clientSocket.getInputStream()
            outputStream = clientSocket.getOutputStream()
         }
    }

    fun readMessage(): Deferred<String> {
        return MainScope().async(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)

             if (bytesRead > 0) String(buffer, 0, bytesRead) else "empty message"
        }

    }

    fun sendMessage(message : String){
        MainScope().launch(Dispatchers.IO) {
            outputStream.write(message.toByteArray())
            outputStream.flush()
        }
    }


    /*private var len: Int = 1024
    private val socket = Socket()
    private val buf = ByteArray(1024)
    var deferred: Deferred<Unit>

    init {
        deferred = MainScope().async {
            try {
                *//**
     * Create a client socket with the host,
     * port, and timeout information.
     *//*

                socket.bind(null)
                socket.connect((InetSocketAddress(hostAddress, 8888)), 500)

                *//**
     * Create a byte stream from a JPEG file and pipe it to the output stream
     * of the socket. This data is retrieved by the server device.
     *//*
                val outputStream = socket.getOutputStream()
                val inputStream: InputStream? = socket.getInputStream()

            } catch (e: FileNotFoundException) {
                //catch logic
            } catch (e: IOException) {
                //catch logic
            } finally {
                *//**
     * Clean up any open sockets when done
     * transferring or if an exception occurred.
     *//*
                socket.takeIf { it.isConnected }?.apply {
                    close()
                }
            }
        }
    }

*/
}