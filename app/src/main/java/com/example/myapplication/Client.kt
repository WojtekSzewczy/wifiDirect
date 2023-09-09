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

class Client(private val carContext: Context, hostAddress: InetAddress, uri: Uri) :
    wifiP2PMessages {
    private val serverPort = 8888
    private val clientSocket = Socket()
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream
    val buf = ByteArray(1024000)
    var len: Int? = null


    init {

        try {
            MainScope().launch(Dispatchers.IO) {
                delay(500L)
                clientSocket.bind(null)
                clientSocket.connect((InetSocketAddress(hostAddress, serverPort)))
                //inputStream = clientSocket.getInputStream()
                outputStream = clientSocket.getOutputStream()

                val cr = carContext.contentResolver
                val inputStream: InputStream? = cr.openInputStream(uri)
                while (inputStream?.read(buf).also { len = it } != -1) {
                    outputStream.write(buf, 0, len!!)
                }
                outputStream.close()
                inputStream?.close()
            }
        } catch (e: FileNotFoundException) {
            //catch logic
        } catch (e: IOException) {
            //catch logic
        } finally {
            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            clientSocket.takeIf { it.isConnected }?.apply {
                close()
            }

        }
    }

    fun readMessage(): Deferred<String> = readMessage(inputStream)

    fun sendMessage(message: String) = sendMessage(message, outputStream)


    /*private var len: Int = 1024
    private val socket = Socket()
    private val buf = ByteArray(1024)
    var deferred: Deferred<Unit>

    init {
        deferred = MainScope().async {
            try {
                */
    /**
     * Create a client socket with the host,
     * port, and timeout information.
     *//*

                socket.bind(null)
                socket.connect((InetSocketAddress(hostAddress, 8888)), 500)

                */
    /**
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
                */
    /**
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