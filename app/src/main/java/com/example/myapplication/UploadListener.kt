package com.example.myapplication

class UploadListener(private var messagingEntity: MessagingInterface) {
    suspend fun listenForUpload(): Boolean {
        while (true) {
            if (messagingEntity.readMessage().await() == "DUBZGO") return true
        }
    }
}