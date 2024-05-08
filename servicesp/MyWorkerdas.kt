package com.xxx.zzz.servicesp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorkerdas(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork called for: " + this.id)

        CommandServicedas.autoStart(context)

        return Result.success()
    }

    override fun onStopped() {
        Log.d(TAG, "onStopped called for: " + this.id)
        super.onStopped()
    }

    companion object {
        private val TAG = MyWorkerdas::class.java.canonicalName
    }
}