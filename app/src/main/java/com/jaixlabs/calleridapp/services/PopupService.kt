package com.jaixlabs.calleridapp.services



import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.hbb20.BuildConfig
import com.jaixlabs.calleridapp.R
import com.jaixlabs.calleridapp.helpers.SwipeDismissLayout


class PopupService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = BuildConfig.APPLICATION_ID + ".PopupServiceChannel"
        private const val TAG = "MADARA"
    }

    private lateinit var windowManager: WindowManager
    private var popupView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val callerName = intent.getStringExtra("callerName")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        var callerProfileImageLink = intent.getStringExtra("callerProfileImageLink")
        var address = intent.getStringExtra("address")
        val isSpamCall = intent.getBooleanExtra("isSpamCall", false)
        val spamType = intent.getStringExtra("spamType")

        callerProfileImageLink = callerProfileImageLink ?: ""
        if (spamType != null && isSpamCall && spamType.isNotEmpty()) {
            address = "$address ($spamType)"
        }

        startForeground(NOTIFICATION_ID, createNotification(callerName, phoneNumber))
        showPopup(callerName, address ?: "", isSpamCall, callerProfileImageLink)
        return START_NOT_STICKY
    }

    private fun showPopup(
        callerName: String?,
        address: String,
        isSpamCall: Boolean,
        profileImageLink: String
    ) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        }

        val tempRoot = FrameLayout(this)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.floating_caller_info, tempRoot, false)

        val floatingCallerInfoMainLayout: SwipeDismissLayout =
            popupView!!.findViewById(R.id.floatingCallerInfoMainLayout)
        val callerNameTV: TextView = popupView!!.findViewById(R.id.callerNameTV)
        val callerAddressTV: TextView = popupView!!.findViewById(R.id.callerAddressTV)
        val callerProfileIV: ShapeableImageView = popupView!!.findViewById(R.id.callerProfileIV)

        callerNameTV.text = callerName
        callerAddressTV.text = address

        if (address.isEmpty()) {
            callerAddressTV.visibility = View.GONE
        }

        if (profileImageLink.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageLink)
                .placeholder(R.drawable.verified_user_24)
                .error(R.drawable.verified_user_24)
                .into(callerProfileIV)
        } else {
            if (isSpamCall) {
                callerProfileIV.setImageResource(R.drawable.warning_24)
            }
        }

        if (isSpamCall) {
            floatingCallerInfoMainLayout.setBackgroundResource(R.drawable.background_danger_floating_caller_info)
        }

        val swipeLayout: SwipeDismissLayout = popupView as SwipeDismissLayout
        swipeLayout.setOnDismissListener {
            windowManager.removeView(popupView)
            stopSelf()
        }

        try {
            windowManager.addView(popupView, params)
        } catch (e: Exception) {
            Log.e(TAG, "showPopup: ", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(callerName: String?, phoneNumber: String?): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(callerName)
            .setContentText(phoneNumber)
            .setSmallIcon(R.drawable.notifications_active_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Popup Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}