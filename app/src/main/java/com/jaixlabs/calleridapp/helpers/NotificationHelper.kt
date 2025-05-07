package com.jaixlabs.calleridapp.helpers


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jaixlabs.calleridapp.MainActivity
import com.jaixlabs.calleridapp.R
import org.json.JSONException
import org.json.JSONObject


object NotificationHelper {

    private const val CHANNEL_ID = "blocked_spam_call_notification_channel"
    private const val CHANNEL_NAME = "Blocked Spam Call Notifications"
    private const val CHANNEL_DESCRIPTION = "This channel is used for showing blocked spam calls notifications"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showBlockedCallNotification(context: Context, callerInfo: JSONObject?, phoneNumber: String) {
        if (callerInfo == null) return

        var callerName = phoneNumber
        var address = phoneNumber

        try {
            if (callerInfo.has("callerName")) {
                callerName = callerInfo.getString("callerName")
            }

            if (callerInfo.has("address")) {
                address = callerInfo.getString("address")
            }
        } catch (e: JSONException) {
            Log.d("MADARA", "showBlockedCallNotification: ${e.message}")
        }

        val notificationTitle = "Blocked Spam Call ($phoneNumber)"
        val notificationText = "$callerName ($address)"

        createNotificationChannel(context) // Notification channel should be created before showing the notification.

        // Create an Intent to launch when the notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.block_24) // Replace with your own icon
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Automatically remove the notification when it's tapped

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return // If permission is not granted, don't show the notification.
        }
        notificationManager.notify(1, builder.build())
    }
}
