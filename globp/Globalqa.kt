package com.xxx.zzz.globp

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.HandlerThread
import com.xxx.zzz.aall.gsonllll.googlepp.GsonBuilderq
import java.lang.ref.WeakReference

object Globalqa {

    @SuppressLint("StaticFieldLeak")
    @JvmStatic
    var mainActivity: WeakReference<Activity?> = WeakReference(null)

    val gson = GsonBuilderq().setPrettyPrinting().create()

    var mainHandler: Handler? = null
        get() {
            if (field == null) {
                field = Handler(mainHandlerThread!!.looper)
            }
            return field
        }
        private set

    var mainHandlerThread: HandlerThread? = null
        get() {
            if (field == null) {
                field = HandlerThread("mainHandlerThread")
                field!!.start()
            }
            return field
        }
        private set
}