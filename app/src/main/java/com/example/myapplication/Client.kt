package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class Client(private val context: Context, hostAddress: InetAddress, uri: Uri) :
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
                initClient(hostAddress)
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

    private suspend fun initClient(hostAddress: InetAddress) {
        delay(500L)
        clientSocket.bind(null)
        clientSocket.connect((InetSocketAddress(hostAddress, serverPort)))
        inputStream = clientSocket.getInputStream()
        outputStream = clientSocket.getOutputStream()
    }

    private fun sendFile(uri: Uri, outputStream: OutputStream) {
        val cr = context.contentResolver
        val inputStream: InputStream? = cr.openInputStream(uri)
        while (inputStream?.read(buf).also { len = it } != -1) {
            outputStream.write(buf, 0, len!!)
        }
        outputStream.close()
        inputStream?.close()
    }

    private fun receiveFile(inputStream: InputStream) {
        val file = createNewFile()
        copyFile(inputStream, FileOutputStream(file))
    }

    private fun createNewFile(): File {
        val file = File(
            Environment.getExternalStorageDirectory().absolutePath + "/${context.packageName}/wifip2pshared-${System.currentTimeMillis()}.jpg"
        )
        val dirs = File(file.parent)

        dirs.takeIf { it.doesNotExist() }?.apply {
            mkdirs()
        }
        file.createNewFile()
        return file
    }

    private fun File.doesNotExist(): Boolean = !exists()

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream): Long {
        try {
            val buffer = ByteArray(1024000)
            var bytesRead: Int
            var totalBytesCopied: Long = 0

            while (inputStream.read(buffer).also {
                    bytesRead = it
                } != -1) {
                Log.v("Server ", "kopiuje")

                outputStream.write(buffer, 0, bytesRead)
                totalBytesCopied += bytesRead
            }

            outputStream.flush()
            return totalBytesCopied
        } catch (e: IOException) {
            // Obsługa błędu odczytu lub zapisu pliku
            e.printStackTrace()
            return -1 // Zwraca -1, aby wskazać, że wystąpił błąd
        } finally {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
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