package com.jaixlabs.calleridapp.helpers



import android.content.Context
import android.content.SharedPreferences

class LoginSaverPrefHelper(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("login_data", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    // Save and get API key
    fun saveApiKey(apiKey: String) {
        editor.putString("api_key", apiKey)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    fun getApiKey(): String {
        return preferences.getString("api_key", "") ?: ""
    }

    // Save and get country name code
    fun saveCountryNameCode(countryNameCode: String) {
        editor.putString("country_name_code", countryNameCode)
        editor.apply()
    }

    fun getCountryNameCode(): String {
        return preferences.getString("country_name_code", "IN") ?: "IN"
    }

    // Save and get number
    fun saveNumber(number: String) {
        editor.putString("number", number)
        editor.apply()
    }

    fun getNumber(): String {
        return preferences.getString("number", "") ?: ""
    }

    // Save and get dialing code
    fun saveDialingCode(dialingCode: Int) {
        editor.putInt("dialing_code", dialingCode)
        editor.apply()
    }

    fun getDialingCode(): Int {
        return preferences.getInt("dialing_code", 91)
    }

    // Save and get OTP request ID
    fun saveOTPRequestId(requestId: String) {
        editor.putString("otp_request_id", requestId)
        editor.apply()
    }

    fun getOTPRequestId(): String {
        return preferences.getString("otp_request_id", "") ?: ""
    }
}
