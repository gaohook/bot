package com.xxx.zzz.smsp

import android.app.Service
import android.content.Intent

import android.os.IBinder


class SmsHeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
