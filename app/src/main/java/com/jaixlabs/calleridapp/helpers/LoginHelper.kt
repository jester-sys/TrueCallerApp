package com.jaixlabs.calleridapp.helpers



import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class LoginHelper(private val context: Context) {

    private val TAG = "MADARA"

    interface OnOTPSentListener {
        fun onSuccess(data: JSONObject)
        fun onFailure(errorMessage: String)
    }

    interface OnOTPVerifiedListener {
        fun onComplete(isVerified: Boolean, message: String)
    }

    fun requestOtp(justNumber: String, dialingCode: Int, countryNameCode: String, listener: OnOTPSentListener) {

        val sendOTPHelper = SendOTPHelper(context, justNumber, countryNameCode, dialingCode)

        sendOTPHelper.sendOTP(object : SendOTPHelper.OnDataRetrievedListener {
            override fun onSuccess(response: String) {
                try {
                    val responseObject = JSONObject(response)

                    if (responseObject.has("status")) {
                        val status = responseObject.getInt("status")

                        when (status) {
                            1, 9 -> {
                                val requestId = responseObject.getString("requestId")

                                val data = JSONObject().apply {
                                    put("countryCode", countryNameCode)
                                    put("dialingCode", dialingCode)
                                    put("phoneNumber", justNumber)
                                    put("requestId", requestId)
                                }

                                listener.onSuccess(data)
                            }
                            5, 6 -> {
                                val message = responseObject.optString("message", "Too many request attempted. Try again after 1 hour later.")
                                listener.onFailure(message)
                            }
                            3 -> {
                                val installationId = responseObject.optString("installationId", null)
                                if (installationId != null) {
                                    val loginSaverPrefHelper = LoginSaverPrefHelper(context)
                                    loginSaverPrefHelper.saveApiKey(installationId)

                                    val data = JSONObject().apply {
                                        put("alreadyLoggedIn", true)
                                    }
                                    listener.onSuccess(data)
                                } else {
                                    listener.onFailure("Something went wrong.\n\n$responseObject")
                                }
                            }
                            else -> {
                                val message = responseObject.optString("message", responseObject.toString())
                                listener.onFailure(message)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Send OTP onSuccess: ", e)
                    listener.onFailure(e.message ?: "Unknown error")
                }
            }

            override fun onFailure(errorMessage: String) {
                Log.d(TAG, "Send OTP onFailure: $errorMessage")
                listener.onFailure("Try again 1 hour later.")
            }
        })
    }

    // ----------------------------------------------------------------------------------------------

    fun verifyOtp(data: JSONObject, listener: OnOTPVerifiedListener) {
        val verifyOTPHelper = VerifyOTPHelper(data)

        verifyOTPHelper.verifyOTP(object : VerifyOTPHelper.OnDataRetrievedListener {
            override fun onSuccess(response: String) {
                try {
                    val responseObject = JSONObject(response)

                    if (responseObject.has("status")) {
                        val status = responseObject.getInt("status")

                        when (status) {
                            2 -> {
                                val suspended = responseObject.optBoolean("suspended", false)
                                if (suspended) {
                                    listener.onComplete(false, "Your account has been suspended!")
                                } else {
                                    val installationId = responseObject.optString("installationId", null)
                                    if (installationId != null) {
                                        listener.onComplete(true, installationId)
                                    } else {
                                        listener.onComplete(false, "Installation ID not found.\n\n$responseObject")
                                    }
                                }
                            }
                            11, 40101 -> listener.onComplete(false, "Invalid OTP")
                            7 -> listener.onComplete(false, "Retries limit exceeded")
                            17 -> {
                                verifyOTPHelper.completeOnboarding(data, object : VerifyOTPHelper.OnDataRetrievedListener {
                                    override fun onSuccess(response: String) {
                                        try {
                                            val responseObject = JSONObject(response)
                                            val installationId = responseObject.optString("installationId", null)
                                            if (installationId != null) {
                                                listener.onComplete(true, installationId)
                                            } else {
                                                listener.onComplete(false, "Installation Id not found. Failed completing signup.\n\n$responseObject")
                                            }
                                        } catch (e: JSONException) {
                                            Log.e(TAG, "onSuccess: ", e)
                                            listener.onComplete(false, e.message ?: "Unknown error")
                                        }
                                    }

                                    override fun onFailure(errorMessage: String) {
                                        Log.d(TAG, "Verify OTP onFailure: $errorMessage")
                                        listener.onComplete(false, errorMessage)
                                    }
                                })
                            }
                            else -> listener.onComplete(false, "Failed to verify OTP \n\n$responseObject")
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Verify OTP onSuccess: ", e)
                    listener.onComplete(false, e.message ?: "Unknown error")
                }
            }

            override fun onFailure(errorMessage: String) {
                Log.d(TAG, "Verify OTP onFailure: $errorMessage")
                listener.onComplete(false, errorMessage)
            }
        })
    }

    // ----------------------------------------------------------------------------------------------
}
