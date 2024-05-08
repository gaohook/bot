package com.xxx.zzz.smsp

import android.app.Activity
import android.os.Bundle
import android.telephony.SmsManager

class SmsSendSms : Activity() {
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SmsManager.getDefault()
    }

}
