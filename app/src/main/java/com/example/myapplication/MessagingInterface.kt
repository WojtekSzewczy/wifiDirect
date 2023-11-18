package com.example.myapplication

import android.net.Uri
import kotlinx.coroutines.Deferred

interface MessagingInterface {
    fun sendFile(uri: Uri)
    fun receiveFile()
    fun readMessage(): Deferred<String>
    fun sendMessage(message: String)
}