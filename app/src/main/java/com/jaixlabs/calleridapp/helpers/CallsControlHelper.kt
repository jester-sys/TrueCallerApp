package com.jaixlabs.calleridapp.helpers

import android.content.Context
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CallsControlHelper(
    private val callScreeningService: CallScreeningService,
    private val callDetails: Call.Details,
    private val phoneNumber: String
) {

    private val context: Context = callScreeningService.applicationContext
    private val countryNameCode: String

    interface OnDataReceivedListener {
        fun onReceived(callerInfo: JSONObject?)
    }

    interface OnTaskCompletedListener {
        fun onTaskCompleted(isSuccessful: Boolean, callerInfo: JSONObject?)
    }

    init {
        val loginSaverPrefHelper = LoginSaverPrefHelper(context)
        countryNameCode = loginSaverPrefHelper.getCountryNameCode()
    }

    // ----------------------------------------------------------------------------------------------

    fun blockAllSpamCalls(
        response: CallScreeningService.CallResponse.Builder,
        listener: OnTaskCompletedListener
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
                response.setDisallowCall(true)
                response.setRejectCall(true)
                callScreeningService.respondToCall(callDetails, response.build())
                listener.onTaskCompleted(true, null)
                return
            }
        }

        getCallerInfo { callerInfo ->
            callerInfo?.let {
                if (it.has("isSpamCall")) {
                    response.setDisallowCall(true)
                    response.setRejectCall(true)
                    callScreeningService.respondToCall(callDetails, response.build())
                    listener.onTaskCompleted(true, it)
                } else {
                    listener.onTaskCompleted(false, it)
                }
            } ?: listener.onTaskCompleted(false, null)
        }
    }

    // ----------------------------------------------------------------------------------------------

    fun blockTopSpamCalls(
        response: CallScreeningService.CallResponse.Builder,
        listener: OnTaskCompletedListener
    ) {
        getCallerInfo { callerInfo ->
            callerInfo?.let {
                if (it.has("spamType")) {
                    try {
                        val spamType = it.getString("spamType")
                        if (spamType.toLowerCase().contains("top")) {
                            response.setDisallowCall(true)
                            response.setRejectCall(true)
                            callScreeningService.respondToCall(callDetails, response.build())
                            listener.onTaskCompleted(true, it)
                        } else {
                            listener.onTaskCompleted(false, it)
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "blockTopSpamCalls: ", e)
                        listener.onTaskCompleted(false, null)
                    }
                } else {
                    listener.onTaskCompleted(false, it)
                }
            } ?: listener.onTaskCompleted(false, null)
        }
    }

    // ----------------------------------------------------------------------------------------------

    private fun getCallerInfo(listener: OnDataReceivedListener) {
        val getPhoneNumberInfo = GetPhoneNumberInfo(context, phoneNumber, countryNameCode)

        getPhoneNumberInfo.getNumberInfo { isSuccessful, message, numberInfo ->
            if (isSuccessful) {
                try {
                    val callerInfo = JSONObject()

                    val data = numberInfo.getJSONArray("data")
                    val firstData = data.getJSONObject(0)

                    if (firstData.has("name")) {
                        val callerName = firstData.getString("name")
                        callerInfo.put("callerName", callerName)
                    } else {
                        callerInfo.put("callerName", phoneNumber)
                    }

                    if (firstData.has("image")) {
                        val callerProfileImageLink = firstData.getString("image")
                        callerInfo.put("callerProfileImageLink", callerProfileImageLink)
                    }

                    if (firstData.has("addresses")) {
                        val addresses = firstData.getJSONArray("addresses")
                        if (addresses.length() > 0) {
                            val addressObj = addresses.getJSONObject(0)
                            if (addressObj.has("city")) {
                                var address = addressObj.getString("city")
                                if (addressObj.has("countryCode")) {
                                    val countryCode = addressObj.getString("countryCode")
                                    val countryName = CustomMethods.getCountryNameByCountryNameCode(countryCode)
                                    address += ", $countryName"
                                }
                                callerInfo.put("address", address)
                            }
                        }
                    }

                    if (firstData.has("spamInfo")) {
                        callerInfo.put("isSpamCall", true)
                        val spamInfo = firstData.getJSONObject("spamInfo")
                        if (spamInfo.has("spamScore")) {
                            val spamScore = spamInfo.getInt("spamScore")
                            callerInfo.put("spamScore", spamScore)
                        }
                        if (spamInfo.has("spamType")) {
                            val spamType = spamInfo.getString("spamType")
                            callerInfo.put("spamType", spamType)
                        }
                    }

                    listener.onReceived(callerInfo)
                } catch (e: JSONException) {
                    Log.e(TAG, "blockAllSpamCalls: ", e)
                    listener.onReceived(null)
                }
            } else {
                listener.onReceived(null)
                if (message.toLowerCase().contains("unauthorized")) {
                    val loginSaverPrefHelper = LoginSaverPrefHelper(context)
                    loginSaverPrefHelper.saveApiKey("")
                }
            }
        }
    }

    companion object {
        private const val TAG = "MADARA"
    }
}
