package com.example.myapplication

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

interface wifiP2PMessages {
    fun sendMessage(message : String, outputStream: OutputStream){
        MainScope().launch(Dispatchers.IO) {
            outputStream.write(message.toByteArray())
            outputStream.flush()
        }
    }

    fun readMessage(inputStream: InputStream): Deferred<String> {
        return MainScope().async(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead > 0) String(buffer, 0, bytesRead) else "empty message"
        }
    }
}