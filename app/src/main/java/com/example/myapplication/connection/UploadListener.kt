package com.example.myapplication.connection

import android.util.Log
import com.example.myapplication.messaging.MessagingInterface
import kotlinx.coroutines.delay

class UploadListener(private var messagingEntity: MessagingInterface) {
    suspend fun listenForUpload(): Boolean {
        delay(100L)
        Log.v("UploadListener", "Listen for upload")
        return messagingEntity.readMessage().await() == "DUBZGO"

    }
}