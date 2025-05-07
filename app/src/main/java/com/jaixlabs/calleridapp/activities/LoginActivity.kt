package com.jaixlabs.calleridapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.appbar.MaterialToolbar
import com.jaixlabs.calleridapp.R
import com.jaixlabs.calleridapp.databinding.ActivityLoginBinding
import com.jaixlabs.calleridapp.helpers.CustomMethods
import com.jaixlabs.calleridapp.helpers.LoginHelper
import com.jaixlabs.calleridapp.helpers.LoginSaverPrefHelper
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginSaverPrefHelper: LoginSaverPrefHelper
    private val TAG = "MADARA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_include)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.login)
        toolbar.titleCentered = true
        toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.arrow_back_24)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loginSaverPrefHelper = LoginSaverPrefHelper(this)

        binding.ccp.registerCarrierNumberEditText(binding.phoneEditText)

        binding.getOtpButton.setOnClickListener {
            val dialingCode = binding.ccp.selectedCountryCode.toInt()
            val countryNameCode = binding.ccp.selectedCountryNameCode
            val fullPhoneNumber = binding.ccp.fullNumberWithPlus
            val justNumber = binding.phoneEditText.text.toString().replace("\\s".toRegex(), "")

            if (CustomMethods.isValidPhoneNumber(fullPhoneNumber)) {
                val pd = ProgressDialog(this).apply {
                    setMessage("Sending OTP...")
                    setCancelable(false)
                    show()
                }

                val loginHelper = LoginHelper(this)

                loginHelper.requestOtp(justNumber, dialingCode, countryNameCode, object : LoginHelper.OnOTPSentListener {
                    override fun onSuccess(data: JSONObject) {
                        pd.dismiss()
                        binding.getOtpContainer.visibility = View.GONE
                        binding.verifyOtpContainer.visibility = View.VISIBLE
                        Toast.makeText(this@LoginActivity, "OTP sent successfully!", Toast.LENGTH_SHORT).show()

                        if (data.has("alreadyLoggedIn")) {
                            Toast.makeText(applicationContext, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                            onBackPressedDispatcher.onBackPressed()
                            finish()
                            return
                        }

                        val requestId = data.optString("requestId")
                        loginSaverPrefHelper.saveOTPRequestId(requestId)
                        loginSaverPrefHelper.saveNumber(justNumber)
                        loginSaverPrefHelper.saveDialingCode(dialingCode)
                        loginSaverPrefHelper.saveCountryNameCode(countryNameCode)
                    }

                    override fun onFailure(errorMessage: String) {
                        pd.dismiss()
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        CustomMethods.errorAlert(this@LoginActivity, "Error", errorMessage, "OK", false)
                    }
                })
            } else {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        binding.verifyOtpButton.setOnClickListener {
            val otp = binding.otpEditText.text.toString().trim()

            if (CustomMethods.isValidOTP(otp)) {
                val pd = ProgressDialog(this).apply {
                    setMessage("Verifying OTP...")
                    setCancelable(false)
                    show()
                }

                try {
                    val data = JSONObject().apply {
                        put("token", otp)
                        put("requestId", loginSaverPrefHelper.getOTPRequestId())
                        put("phoneNumber", loginSaverPrefHelper.getNumber())
                        put("dialingCode", loginSaverPrefHelper.getDialingCode())
                        put("countryCode", loginSaverPrefHelper.getCountryNameCode())
                    }

                    val loginHelper = LoginHelper(this)

                    loginHelper.verifyOtp(data) { isVerified, message ->
                        pd.dismiss()
                        if (isVerified) {
                            loginSaverPrefHelper.saveApiKey(message)
                            Toast.makeText(applicationContext, "OTP verified successfully!", Toast.LENGTH_SHORT).show()
                            onBackPressedDispatcher.onBackPressed()
                            finish()
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            CustomMethods.errorAlert(this, "Error", message, "OK", false)
                        }
                    }

                } catch (e: JSONException) {
                    Log.e(TAG, "onCreate: ", e)
                    pd.dismiss()
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    CustomMethods.errorAlert(this, "Error", e.message ?: "Unknown error", "OK", false)
                }
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
