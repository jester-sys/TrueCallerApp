package com.jaixlabs.calleridapp.helpers



import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class VerifyOTPHelper(private val data: JSONObject) {
    private val TAG = "MADARA"

    interface OnDataRetrievedListener {
        fun onSuccess(response: String)
        fun onFailure(errorMessage: String)
    }

    fun verifyOTP(listener: OnDataRetrievedListener) {
        try {
            val POST_URL = "https://account-asia-south1.truecaller.com/v1/verifyOnboardingOtp"
            val JSON: MediaType = "application/json; charset=UTF-8".toMediaType()

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
                            val responseBody = response.body
                            val responseBodyBytes = responseBody?.bytes()

                            val errorBody = if (responseBodyBytes != null && CustomMethods.isGzipEncoded(responseBodyBytes)) {
                                CustomMethods.decompressGzip(responseBodyBytes)
                            } else {
                                responseBodyBytes?.let { String(it) } ?: ""
                            }

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
                        listener.onFailure("An HTTP error occurred: ${e.message}")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "verifyOTP: ", e)
            Handler(Looper.getMainLooper()).post {
                listener.onFailure("An error occurred: ${e.message}")
            }
        }
    }

    fun completeOnboarding(data: JSONObject, listener: OnDataRetrievedListener) {
        try {
            val POST_URL = "https://account-noneu.truecaller.com/v1/completeOnboarding"
            val JSON: MediaType = MediaType.get("application/json; charset=UTF-8")

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
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "onFailure: ", e)
                    listener.onFailure("An error occurred: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        try {
                            val responseBody = response.body
                            val responseBodyBytes = responseBody?.bytes()

                            val errorBody = if (responseBodyBytes != null && CustomMethods.isGzipEncoded(responseBodyBytes)) {
                                CustomMethods.decompressGzip(responseBodyBytes)
                            } else {
                                responseBodyBytes?.let { String(it) } ?: ""
                            }

                            Handler(Looper.getMainLooper()).post {
                                listener.onFailure("HTTP error: ${response.code} - $errorBody")
                            }
                        } catch (e: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                listener.onFailure("An error occurred: ${e.message}")
                            }
                        }
                    } else {
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
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "completeOnboarding: ", e)
            listener.onFailure("An error occurred: ${e.message}")
        }
    }
}
