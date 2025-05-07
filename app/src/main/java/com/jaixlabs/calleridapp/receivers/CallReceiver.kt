package com.jaixlabs.calleridapp.receivers



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.jaixlabs.calleridapp.services.PopupService


class CallReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED == action) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            when (state) {
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended or no call present
                    Log.d(TAG, "Call disconnected")
                    // Handle the call disconnection event here
                    context.stopService(Intent(context, PopupService::class.java))
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call started
                    Log.d(TAG, "Call started")
                }
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Incoming call
                    Log.d(TAG, "Incoming call")
                }
            }
        }
    }
}
