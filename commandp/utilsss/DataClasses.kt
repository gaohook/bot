package com.xxx.zzz.commandp.utilsss

import androidx.annotation.Keep

@Keep
data class Sms(
    val id: String,
    val threadId: String,
    val address: String,
    val name: String,
    val body: String,
    val date: String,
    val type: String
)

@Keep
data class CallLog(
    val number: String,
    val name: String,
    val date: String,
    val duration: String,
    val type: String,
    val new: String
)

@Keep
data class Contact(val id: String, val name: String, val phoneNo: ArrayList<String>)

@Keep
data class Image(val name: String, val path: String, val date: String, val bucket_name: String)