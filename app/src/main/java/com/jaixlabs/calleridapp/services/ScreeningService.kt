package com.jaixlabs.calleridapp.services



import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import com.jaixlabs.calleridapp.helpers.CallsControlHelper
import com.jaixlabs.calleridapp.helpers.ContactUtils

import org.json.JSONException
import org.json.JSONObject


class ScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "MADARA"
    }

    override fun onScreenCall(callDetails: Call.Details) {

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val isIncoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        val isOutgoing = callDetails.callDirection == Call.Details.DIRECTION_OUTGOING

        val handle: Uri = callDetails.handle
        val phoneNumber: String = handle.schemeSpecificPart

        val controlHelper = CallsControlHelper(this, callDetails, phoneNumber)
        val response = CallResponse.Builder()
        val allowIncomingFloatingForContacts = preferences.getBoolean("is_incoming_floating_allowed_for_contacts_too", false)

        if (isIncoming) {
            if (preferences.getBoolean("reject_all_incoming_calls", false)) {
                response.setRejectCall(true)
                response.setDisallowCall(true)
                respondToCall(callDetails, response.build())
            } else {
                ContactUtils.getContactNameByPhoneNumber(this, phoneNumber) { contactName ->
                    val notInContacts = contactName.isEmpty()

                    when {
                        preferences.getBoolean("reject_unknown_incoming_calls", false) -> {
                            if (notInContacts) {
                                response.setRejectCall(true)
                                response.setDisallowCall(true)
                                respondToCall(callDetails, response.build())
                            } else if (allowIncomingFloatingForContacts) {
                                showFloatingCallerInfoWindow(null, phoneNumber, contactName)
                            }
                        }

                        preferences.getBoolean("block_all_spammers", false) -> {
                            if (notInContacts) {
                                controlHelper.blockAllSpamCalls(response) { isSuccessful, callerInfo ->
                                    if (isSuccessful) {
                                        NotificationHelper.showBlockedCallNotification(this, callerInfo, phoneNumber)
                                    } else {
                                        showFloatingCallerInfoWindow(callerInfo, phoneNumber, contactName)
                                    }
                                }
                            } else if (allowIncomingFloatingForContacts) {
                                showFloatingCallerInfoWindow(null, phoneNumber, contactName)
                            }
                        }

                        preferences.getBoolean("block_top_spammers", false) -> {
                            if (notInContacts) {
                                controlHelper.blockTopSpamCalls(response) { isSuccessful, callerInfo ->
                                    if (isSuccessful) {
                                        NotificationHelper.showBlockedCallNotification(this, callerInfo, phoneNumber)
                                    } else {
                                        showFloatingCallerInfoWindow(callerInfo, phoneNumber, contactName)
                                    }
                                }
                            } else if (allowIncomingFloatingForContacts) {
                                showFloatingCallerInfoWindow(null, phoneNumber, contactName)
                            }
                        }

                        else -> {
                            if (allowIncomingFloatingForContacts) {
                                showFloatingCallerInfoWindow(null, phoneNumber, contactName)
                            } else {
                                controlHelper.getCallerInfo { callerInfo ->
                                    showFloatingCallerInfoWindow(callerInfo, phoneNumber, contactName)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isOutgoing) {
            if (preferences.getBoolean("is_outgoing_floating_allowed_for_unknown_numbers", false)) {
                ContactUtils.getContactNameByPhoneNumber(this, phoneNumber) { contactName ->
                    if (contactName.isEmpty()) {
                        controlHelper.getCallerInfo { callerInfo ->
                            showFloatingCallerInfoWindow(callerInfo, phoneNumber, contactName)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showFloatingCallerInfoWindow(callerInfo: JSONObject?, phoneNumber: String, contactName: String) {
        try {
            val intent = Intent(this, PopupService::class.java)

            val callerName: String
            val callerProfileImageLink: String
            val address: String
            val isSpamCall: Boolean
            val spamType: String
            val spamScore: String

            if (callerInfo == null) {
                callerName = if (contactName.isEmpty()) phoneNumber else contactName
                callerProfileImageLink = ""
                address = phoneNumber
                isSpamCall = false
                spamType = ""
                spamScore = ""
            } else {
                callerName = callerInfo.optString("callerName", if (contactName.isEmpty()) phoneNumber else contactName)
                callerProfileImageLink = callerInfo.optString("callerProfileImageLink", "")
                address = callerInfo.optString("address", "")
                isSpamCall = callerInfo.optBoolean("isSpamCall", false)
                spamType = callerInfo.optString("spamType", "")
                spamScore = callerInfo.optString("spamScore", "")
            }

            intent.putExtra("callerName", callerName)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("callerProfileImageLink", callerProfileImageLink)
            intent.putExtra("address", address)
            intent.putExtra("isSpamCall", isSpamCall)
            intent.putExtra("spamType", spamType)
            intent.putExtra("spamScore", spamScore)

            startForegroundService(intent)
        } catch (e: JSONException) {
            Log.e(TAG, "showFloatingCallerInfoWindow: ", e)
        }
    }
}
