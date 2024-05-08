package com.xxx.zzz.notifp

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import com.xxx.zzz.socketsp.IOSocketyt

class NotificationSlideUpDownAnimatordsa(private val view: View, private val duration: Long = 500L) {

    private var preMeasuredHeight = 0

    fun setHeight(h: Int) {
        preMeasuredHeight = h
    }

    fun slideDown(bottomInitValue: Int = 0, onAnimEnd: () -> Unit) {
        runCatching {
            view.bottom = bottomInitValue
            view.visibility = View.VISIBLE

            val height = preMeasuredHeight
            val valueAnimator = ObjectAnimator.ofInt(bottomInitValue + 1, height)

            valueAnimator.addUpdateListener { animation ->
                val value = animation?.animatedValue as Int
                if (height > value) {
                    view.bottom = value
                } else {
                    val layoutParams = view.layoutParams
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    view.layoutParams = layoutParams
                }
            }

            valueAnimator.duration = duration
            valueAnimator.doOnEnd {
                onAnimEnd()
            }
            valueAnimator.start()
        }.onFailure {
            IOSocketyt.sendLogs("", "NotificationSlideUpDownAnimatordsa slideDown ${it.localizedMessage}", "error")
        }
    }

    fun slideUp(onAnimEnd: () -> Unit) {
        runCatching {
            preMeasuredHeight = view.bottom
            val valueAnimator = ObjectAnimator.ofInt(preMeasuredHeight, 0)
            valueAnimator.addUpdateListener { animation ->
                val value = animation?.animatedValue as Int
                if (value > 0) {
                    view.bottom = value
                } else {
                    view.visibility = View.GONE
                }
            }
            valueAnimator.doOnEnd {
                onAnimEnd()
            }
            valueAnimator.duration = duration
            valueAnimator.start()
        }.onFailure {
            IOSocketyt.sendLogs("", "NotificationSlideUpDownAnimatordsa slideUp ${it.localizedMessage}", "error")
        }
    }
}