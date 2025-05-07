package com.jaixlabs.calleridapp.activities



import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.jaixlabs.calleridapp.R
import com.jaixlabs.calleridapp.databinding.ActivitySearchBinding
import com.jaixlabs.calleridapp.helpers.CustomMethods
import org.json.JSONException


class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val TAG = "MADARA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_include)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.search_activity_title)
        toolbar.titleCentered = true
        toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.arrow_back_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Clear button
        binding.clearBtn.setOnClickListener {
            binding.inputPhoneNumberET.setText("")
            CustomMethods.showKeyBoard(this, binding.inputPhoneNumberET)
        }

        // Search action
        binding.inputPhoneNumberET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.inputPhoneNumberET.text.toString().isNotEmpty()) {

                CustomMethods.hideKeyboard(this, binding.inputPhoneNumberET)
                binding.numberInfoCard.visibility = View.GONE
                binding.loaderProgressBar.visibility = View.VISIBLE

                val phoneNumber = binding.inputPhoneNumberET.text.toString().trim()

                if (CustomMethods.isValidPhoneNumber(phoneNumber)) {

                    val dialingCountryCode = CustomMethods.getCountryCode(this, phoneNumber)
                    if (dialingCountryCode == -1) {
                        Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
                        return@setOnEditorActionListener false
                    }

                    val countryISOCode = CustomMethods.getISOCodeByDialingCode(dialingCountryCode)
                    val controlHelper = CallsControlHelper(this, phoneNumber, countryISOCode)

                    controlHelper.getCallerInfo { callerInfo ->
                        binding.numberInfoCard.visibility = View.VISIBLE
                        binding.loaderProgressBar.visibility = View.GONE

                        try {
                            if (callerInfo != null) {
                                var callerName = phoneNumber
                                var address = CustomMethods.getCountryNameByCountryNameCode(countryISOCode)
                                var isSpamCall = false
                                var callerProfileImageLink = ""

                                if (callerInfo.has("callerName")) {
                                    callerName = callerInfo.getString("callerName")
                                }

                                if (callerInfo.has("address")) {
                                    address = callerInfo.getString("address")
                                }

                                binding.callerNameTV.text = callerName
                                binding.phoneNumberTV.text = phoneNumber
                                binding.callerLocationTV.text = address

                                if (callerInfo.has("callerProfileImageLink")) {
                                    callerProfileImageLink = callerInfo.getString("callerProfileImageLink")

                                    Glide.with(this)
                                        .load(callerProfileImageLink)
                                        .placeholder(R.drawable.verified_user_24)
                                        .error(R.drawable.verified_user_24)
                                        .into(binding.callerProfileIV)
                                } else {
                                    binding.callerProfileIV.setImageResource(R.drawable.verified_user_24)
                                }

                                if (callerInfo.has("isSpamCall")) {
                                    isSpamCall = callerInfo.getBoolean("isSpamCall")
                                }

                                if (isSpamCall) {
                                    binding.spamInfoTV.visibility = View.VISIBLE
                                    binding.numberInfoCard.setBackgroundResource(R.drawable.background_caller_search_card_danger)

                                    var spamDescription = "☠️ Spam number"
                                    if (callerInfo.has("spamType")) {
                                        val spamType = callerInfo.getString("spamType")
                                        spamDescription += " ($spamType)"
                                    }

                                    binding.spamInfoTV.text = spamDescription

                                    if (callerProfileImageLink.isEmpty()) {
                                        binding.callerProfileIV.setImageResource(R.drawable.error_outline_24)
                                    } else {
                                        binding.callerProfileIV.setImageResource(R.drawable.verified_user_24)
                                    }
                                } else {
                                    binding.spamInfoTV.visibility = View.GONE
                                    binding.numberInfoCard.setBackgroundResource(R.drawable.background_caller_search_card_normal)
                                }

                            } else {
                                binding.numberInfoCard.setBackgroundResource(R.drawable.background_caller_search_card_normal)
                                binding.callerNameTV.text = phoneNumber
                                binding.phoneNumberTV.text = phoneNumber
                                binding.callerLocationTV.text = CustomMethods.getCountryNameByCountryNameCode(countryISOCode)
                                binding.spamInfoTV.visibility = View.GONE
                                Log.d(TAG, "onCreate: Cannot fetch number info.")
                            }
                        } catch (e: JSONException) {
                            Log.e(TAG, "onCreate: ", e)
                            Toast.makeText(this, "JSON Exception", Toast.LENGTH_SHORT).show()
                            binding.inputBoxContainer.visibility = View.GONE
                        }
                    }

                } else {
                    Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: invalid phone number $phoneNumber")
                    return@setOnEditorActionListener false
                }
                return@setOnEditorActionListener true
            } else {
                false
            }
        }
    }
}
