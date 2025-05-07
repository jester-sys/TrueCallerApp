package com.jaixlabs.calleridapp.helpers



import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.jaixlabs.calleridapp.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class GetPhoneNumberInfo(
    private val context: Context,
    private val phoneNumber: String,
    private val countryNameCode: String
) {

    private val TAG = "MADARA"
    private val apiKey: String

    init {
        val loginSaverPrefHelper = LoginSaverPrefHelper(context)
        apiKey = loginSaverPrefHelper.apiKey
    }

    interface OnFetchedInfoListener {
        fun onReceivedResponse(isSuccessful: Boolean, message: String, numberInfo: JSONObject?)
    }

    fun getNumberInfo(listener: OnFetchedInfoListener) {

        if (apiKey.isEmpty() || !CustomMethods.isInternetAvailable(context)) {
            listener.onReceivedResponse(false, "API key is empty or internet is not available", null)
            return
        }

        val request = Request.Builder()
            .url("https://search5-noneu.truecaller.com/v2/search?q=$phoneNumber&countryCode=$countryNameCode&type=4&locAddr=&encoding=json")
            .addHeader("accept", "application/json")
            .addHeader("authorization", "Bearer $apiKey")
            .addHeader("accept-encoding", "gzip")
            .addHeader("user-agent", context.getString(R.string.truecaller_user_agent))
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: ", e)
                Handler(Looper.getMainLooper()).post {
                    listener.onReceivedResponse(false, e.message ?: "Unknown error", null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d(TAG, "onResponse unsuccessful: ${response.code}")

                    if (response.code == 401) {
                        Handler(Looper.getMainLooper()).post {
                            listener.onReceivedResponse(false, "Unauthorized", null)
                        }
                    } else if (response.code == 429) {
                        Handler(Looper.getMainLooper()).post {
                            listener.onReceivedResponse(false, "Too many requests. Retry after sometimes.", null)
                            Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            listener.onReceivedResponse(false, "Response code: ${response.code}", null)
                        }
                    }
                    return
                }

                val responseBody = response.body

                responseBody?.let {
                    val responseBodyBytes = it.bytes()

                    val responseString = if (CustomMethods.isGzipEncoded(responseBodyBytes)) {
                        CustomMethods.decompressGzip(responseBodyBytes)
                    } else {
                        String(responseBodyBytes)
                    }

                    try {
                        val numberInfo = JSONObject(responseString)
                        Handler(Looper.getMainLooper()).post {
                            listener.onReceivedResponse(true, "Success", numberInfo)
                        }
                    } catch (e: JSONException) {
                        listener.onReceivedResponse(false, responseString, null)
                        Log.e(TAG, "onResponse: ", e)
                    }
                }
            }
        })
    }
}
