package com.xxx.zzz.globp

import org.json.JSONObject
import java.util.*

object Stringsvcx {
    fun localeTextAccessibility(): String? {
        return try {
            val jsonObject = JSONObject(constNm.l3)
            jsonObject.getString(Locale.getDefault().language)
        } catch (ex: Exception) {
            "Enable"
        }
    }
}