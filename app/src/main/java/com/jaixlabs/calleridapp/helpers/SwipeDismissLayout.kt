package com.jaixlabs.calleridapp.helpers



import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout

class SwipeDismissLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private var startX = 0f
    private var dX = 0f
    private var dismissListener: OnDismissListener? = null

    interface OnDismissListener {
        fun onDismiss()
    }

    fun setOnDismissListener(listener: () -> Unit) {
        dismissListener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
            }
            MotionEvent.ACTION_MOVE -> {
                dX = event.rawX - startX
                translationX = dX
            }
            MotionEvent.ACTION_UP -> {
                if (Math.abs(dX) > width / 4) {
                    dismissListener?.onDismiss()
                } else {
                    animate().translationX(0f).duration = 100
                }
            }
        }
        return true
    }
}
