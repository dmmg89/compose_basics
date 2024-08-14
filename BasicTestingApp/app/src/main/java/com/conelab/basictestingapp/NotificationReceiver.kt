package com.conelab.basictestingapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("title")
        val message = intent?.getStringExtra("message")

        if (context != null && title != null && message != null) {
            createNotificationChannel(context) // Crear el canal de notificación
            showNotification(context, title, message)
        }
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel"
            val channelName = "Default Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Este es el canal predeterminado para las notificaciones"
            }
            // Registrar el canal con el sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(androidx.core.R.drawable.ic_call_answer) // Cambia esto al ícono que quieras usar
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        // Verificar el permiso POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(0, notificationBuilder.build())
        }
    }
}
