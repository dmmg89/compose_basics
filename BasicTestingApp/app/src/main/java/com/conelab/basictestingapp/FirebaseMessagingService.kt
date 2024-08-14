package com.conelab.basictestingapp

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Si el mensaje tiene un payload de datos.
        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"] ?: "Default Title"
            val message = remoteMessage.data["message"] ?: "Default Message"
            sendBroadcastNotification(title, message)
        }

        // Si el mensaje contiene una notificación.
        remoteMessage.notification?.let {
            val title = it.title ?: "Default Title"
            val message = it.body ?: "Default Message"
            sendBroadcastNotification(title, message)
        }
    }

    private fun sendBroadcastNotification(title: String, message: String) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = "com.conelab.basictestingapp.NOTIFICATION"
            putExtra("title", title)
            putExtra("message", message)
        }
        sendBroadcast(intent)
    }
}