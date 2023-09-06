package com.example.myapplication

import android.content.Context
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
class FileServerAsyncTask (private val context: Context):wifiP2PMessages{
    private val serverPort = 8888

    private lateinit var serverSocket : ServerSocket
    private lateinit var clientSocket : Socket
    private lateinit var inputStream : InputStream
    private lateinit var outputStream : OutputStream

    init{
        MainScope().launch(Dispatchers.IO) {
            serverSocket = ServerSocket(serverPort)
            clientSocket = serverSocket.accept()
            inputStream = clientSocket.getInputStream()
            outputStream = clientSocket.getOutputStream()
        }
    }

    fun readMessage(): Deferred<String> = readMessage(inputStream)

    fun sendMessage(message : String) = sendMessage(message ,outputStream)


    /* private val maninScope = MainScope()
     var deferred : Deferred<String>
     init{
         Log.v(DEFAULT_TAG, "init server")
         deferred = maninScope.async(Dispatchers.IO){
             Log.v(DEFAULT_TAG, "Start")
             val serverSocket = ServerSocket(8888)
             serverSocket.use {
                 Log.v(DEFAULT_TAG, "server socket")

                 val client = it.accept()
                 val file = File(Environment.getExternalStorageDirectory().absolutePath +
                         "/${context.packageName}/wifip2pshared-${System.currentTimeMillis()}.jpg")
                 val dirs = file.parent?.let { it1 -> File(it1) }
                 Log.v(DEFAULT_TAG, "server socket")

                 dirs.takeIf {file ->
                     file!!.doesNotExist() }?.apply {
                     mkdirs()
                 }
                 file.createNewFile()
                 val inputStream = client.getInputStream()
                 copyFile(inputStream, FileOutputStream(file))
                 serverSocket.close()
                 file.absolutePath
             }

         }
     }
     private fun File.doesNotExist():Boolean = !exists()
     private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
         val buffer = ByteArray(1024)
         var bytesRead: Int

         while (inputStream.read(buffer).also { bytesRead = it } != -1) {
             outputStream.write(buffer, 0, bytesRead)
         }

         outputStream.flush()
         outputStream.close()
         inputStream.close()
     }*/


}

