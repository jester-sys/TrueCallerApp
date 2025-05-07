package com.jaixlabs.calleridapp.helpers

import okhttp3.MediaType.Companion.toMediaType

package zorro.dimyon.calleridentity.helpers

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SendOTPHelper(
    private val context: Context,
    private val phoneNumber: String,
    private val countryCode: String,
    private val dialingCode: Int
) {

    interface OnDataRetrievedListener {
        fun onSuccess(response: String)
        fun onFailure(errorMessage: String)
    }

    fun sendOTP(listener: OnDataRetrievedListener) {
        try {
            val data = JSONObject().apply {
                put("countryCode", countryCode)
                put("dialingCode", dialingCode)

                val installationDetails = JSONObject().apply {
                    val app = JSONObject().apply {
                        put("buildVersion", 5)
                        put("majorVersion", 11)
                        put("minorVersion", 7)
                        put("store", "GOOGLE_PLAY")
                    }
                    put("app", app)

                    val device = JSONObject().apply {
                        put("deviceId", CustomMethods.getDeviceId(context))
                        put("language", "en")
                        put("manufacturer", Build.MANUFACTURER)
                        put("model", Build.MODEL)
                        put("osName", "Android")
                        put("osVersion", "10")
                        put("mobileServices", JSONArray().put("GMS"))
                    }
                    put("device", device)
                    put("language", "en")
                }
                put("installationDetails", installationDetails)
                put("phoneNumber", phoneNumber)
                put("region", "region-2")
                put("sequenceNo", 2)
            }

            Log.d("MADARA", "sendOTP: $data")

            val POST_URL = "https://account-asia-south1.truecaller.com/v2/sendOnboardingOtp"
            val JSON = "application/json; charset=UTF-8".toMediaType()
            val body: RequestBody = RequestBody.create(JSON, data.toString())

            val request = Request.Builder()
                .url(POST_URL)
                .post(body)
                .addHeader("content-type", "application/json; charset=UTF-8")
                .addHeader("accept-encoding", "gzip")
                .addHeader("user-agent", "Truecaller/11.75.5 (Android;10)")
                .addHeader("clientsecret", "lvc22mp3l1sfv6ujg83rd17btt")
                .build()

            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        try {
                            val errorBody = response.body?.string() ?: "No error body"
                            Handler(Looper.getMainLooper()).post {
                                listener.onFailure("HTTP error: ${response.code} - $errorBody")
                            }
                        } catch (e: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                listener.onFailure("An error occurred: ${e.message}")
                            }
                        }
                        return
                    }

                    try {
                        val responseBody = response.body

                        if (responseBody == null) {
                            Handler(Looper.getMainLooper()).post {
                                listener.onFailure("No response body")
                            }
                            return
                        }

                        val responseBodyBytes = responseBody.bytes()
                        val responseString = if (CustomMethods.isGzipEncoded(responseBodyBytes)) {
                            CustomMethods.decompressGzip(responseBodyBytes)
                        } else {
                            String(responseBodyBytes)
                        }

                        Handler(Looper.getMainLooper()).post {
                            listener.onSuccess(responseString)
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            listener.onFailure("An error occurred: ${e.message}")
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        listener.onFailure("An error occurred: ${e.message}")
                    }
                }
            })

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                listener.onFailure("An error occurred: ${e.message}")
            }
        }
    }
}
