package com.mycompany.parkingmanager.domain.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mycompany.parkingmanager.presentation.ui.activities.MainActivity

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        NotificationManager().showNotification(context!!, "A new day!", "Just 10 minutes to start a new day. Don't forget about the report.", pendingIntent)
        // NotificationManager().scheduleDailyNotification(context)

    }
}