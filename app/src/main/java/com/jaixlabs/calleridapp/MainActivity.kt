package com.jaixlabs.calleridapp



import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.jaixlabs.calleridapp.activities.LoginActivity
import com.jaixlabs.calleridapp.activities.SearchActivity
import com.jaixlabs.calleridapp.activities.SettingsActivity
import com.jaixlabs.calleridapp.databinding.ActivityMainBinding
import com.jaixlabs.calleridapp.helpers.LoginSaverPrefHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isUserLoggedIn = false

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE
    )

    private val callScreeningRequest = 153
    private val overlayPermissionRequest = 9786
    private val permissionsRequestCode = 4556

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_include)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.loginWithOtpBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.searchBtn.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val loginPrefHelper = LoginSaverPrefHelper(this)

        if (loginPrefHelper.apiKey.isEmpty()) {
            binding.loginWithOtpBtn.visibility = View.VISIBLE
            binding.afterLoginDesignContainer.visibility = View.GONE
        } else {
            isUserLoggedIn = true
            binding.loginWithOtpBtn.visibility = View.GONE
            binding.afterLoginDesignContainer.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)

        if (!isUserLoggedIn) {
            menu.findItem(R.id.menu_search_action)?.isVisible = false
            menu.findItem(R.id.menu_settings_action)?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings_action -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_search_action -> startActivity(Intent(this, SearchActivity::class.java))
        }
        return true
    }

    private fun checkAndRequestPermissions() {
        if (areAllPermissionsGranted()) {
            requestRole()
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, permissionsRequestCode)
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestRole() {
        val roleManager = getSystemService(ROLE_SERVICE) as? RoleManager
        if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            startActivityForResult(intent, callScreeningRequest)
        } else {
            checkOverlayPermission()
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, overlayPermissionRequest)
        } else {
            requestBatteryOptimization()
        }
    }

    @Suppress("BatteryLife")
    private fun requestBatteryOptimization() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            callScreeningRequest -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "App is now the call screening app.", Toast.LENGTH_SHORT).show()
                    checkOverlayPermission()
                } else {
                    showToastAndFinish("This app needs call screening permission.")
                }
            }

            overlayPermissionRequest -> {
                if (!Settings.canDrawOverlays(this)) {
                    showToastAndFinish("Overlay permission is required.")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionsRequestCode) {
            if (areAllPermissionsGranted()) {
                requestRole()
            } else {
                showToastAndFinish("All permissions are required.")
            }
        }
    }

    private fun showToastAndFinish(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }
}
