package com.example.myapplication.messaging

import android.net.Uri

class MessagingMenager(
    private var messagingEntity: MessagingInterface
) {
    fun sendFile(uri: Uri) {
        messagingEntity.sendFile(uri)
    }

    fun receiveFile() {
        messagingEntity.receiveFile()
    }

    fun sendMessage(message: String) {
        messagingEntity.sendMessage(message)
    }

}