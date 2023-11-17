package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    private fun sendFile(uri: Uri, outputStream: OutputStream, context: Context) {
        val buf = ByteArray(1024000)
        var len: Int? = null

        val cr = context.contentResolver
        val inputStream: InputStream? = cr.openInputStream(uri)
        while (inputStream?.read(buf).also { len = it } != -1) {
            outputStream.write(buf, 0, len!!)
        }
        outputStream.close()
        inputStream?.close()
    }

    private fun receiveFile(inputStream: InputStream, context: Context) {
        val file = createNewFile(context)
        copyFile(inputStream, FileOutputStream(file))
    }

    private fun createNewFile(context: Context): File {
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

}