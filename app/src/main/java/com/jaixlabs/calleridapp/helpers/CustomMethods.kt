package com.jaixlabs.calleridapp.helpers



import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.jaixlabs.calleridapp.R

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.Locale

object CustomMethods {

    private const val TAG = "MADARA"

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Remove country code for validation
        val nationalNumber = phoneNumber.replaceAll("\\D", "")
        // Check if the number is numeric and within the desired length
        return nationalNumber.matches("\\d{7,13}".toRegex())
    }

    fun isValidOTP(otp: String): Boolean {
        return if (otp.length in 4..10) {
            try {
                otp.toInt()
                true
            } catch (ignored: NumberFormatException) {
                false
            }
        } else {
            false
        }
    }

    //--------------------------------------------------------------------------------------------------

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    //--------------------------------------------------------------------------------------------------

    fun isGzipEncoded(bytes: ByteArray): Boolean {
        return bytes.size > 1 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()
    }

    @Throws(IOException::class)
    fun decompressGzip(compressed: ByteArray): String {
        val byteArrayInputStream = ByteArrayInputStream(compressed)
        val gzipInputStream = GZIPInputStream(byteArrayInputStream)
        val out = StringBuilder()
        val buffer = ByteArray(1024)
        var len: Int
        while (gzipInputStream.read(buffer).also { len = it } != -1) {
            out.append(String(buffer, 0, len))
        }
        return out.toString()
    }

    //--------------------------------------------------------------------------------------------------

    fun errorAlert(activity: Activity, errorTitle: String, errorBody: String, actionButton: String, shouldGoBack: Boolean) {
        if (!activity.isFinishing) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(errorTitle)
            builder.setMessage(errorBody)
            builder.setIcon(R.drawable.warning_24)
            builder.setPositiveButton(actionButton) { dialogInterface, _ ->
                if (shouldGoBack) {
                    activity.finish()
                } else {
                    dialogInterface.dismiss()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    //--------------------------------------------------------------------------------------------------

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val capabilities = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities?.run {
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } ?: false
    }

    //--------------------------------------------------------------------------------------------------

    fun getCountryNameByCountryNameCode(countryNameCode: String): String {
        val locale = Locale("", countryNameCode)
        return locale.displayCountry
    }

    // Method to get the country ISO code from the country code
    fun getISOCodeByDialingCode(countryCode: Int): String {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val regionCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode)
        return regionCode.takeIf { it.isNotEmpty() } ?: "IN" // Return default value if invalid
    }

    //--------------------------------------------------------------------------------------------------

    // Method to get the country code from a phone number
    fun getCountryCode(context: Context, phoneNumber: String): Int {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val defaultRegion = getCountryIso(context)

        return try {
            val parsedNumber: Phonenumber.PhoneNumber = phoneNumberUtil.parse(phoneNumber, defaultRegion)
            if (parsedNumber.hasCountryCode()) {
                parsedNumber.countryCode
            } else {
                phoneNumberUtil.getCountryCodeForRegion(defaultRegion)
            }
        } catch (e: NumberParseException) {
            Log.e(TAG, "getCountryCode: ", e)
            -1
        }
    }

    // Method to get the country ISO code from the TelephonyManager
    private fun getCountryIso(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var countryIso = tm.networkCountryIso.uppercase()

        if (countryIso.isEmpty()) {
            // Fallback to the SIM country if the network country is not available
            countryIso = tm.simCountryIso.uppercase()
        }

        return countryIso
    }

    //--------------------------------------------------------------------------------------------------

    fun showKeyBoard(activity: Activity, editText: EditText) {
        if (editText.requestFocus()) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    fun hideKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //--------------------------------------------------------------------------------------------------
}
