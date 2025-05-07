package com.jaixlabs.calleridapp.activities



import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.appbar.MaterialToolbar
import com.jaixlabs.calleridapp.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        // ------------------------------------------------------------------------------------------
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_include)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.settings)

        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_back_24))
        toolbar.setNavigationOnClickListener { onBackPressed() }
        // ------------------------------------------------------------------------------------------
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Setting up dependencies between preferences
            setupDependency("block_all_spammers", "block_top_spammers")
            setupDependency("reject_all_incoming_calls", "reject_unknown_incoming_calls")
        }

        /**
         * Sets up a dependency where enabling the master preference automatically enables
         * and locks the dependent preference.
         */
        private fun setupDependency(masterKey: String, dependentKey: String) {
            val masterPreference: SwitchPreferenceCompat? = findPreference(masterKey)
            val dependentPreference: SwitchPreferenceCompat? = findPreference(dependentKey)

            masterPreference?.let { master ->
                dependentPreference?.let { dependent ->
                    // Initialize the dependent preference state based on the master preference
                    updateDependentState(master.isChecked, dependent)

                    // Listener for changes in the master preference
                    master.setOnPreferenceChangeListener { _, newValue ->
                        val isEnabled = newValue as Boolean
                        updateDependentState(isEnabled, dependent)
                        true // Allow the master preference state change
                    }

                    // Listener for attempts to change the dependent preference
                    dependent.setOnPreferenceChangeListener { _, _ ->
                        // If master is enabled, prevent changes to dependent
                        !master.isChecked
                    }
                }
            }
        }

        /**
         * Updates the state of the dependent preference based on the master preference's state.
         */
        private fun updateDependentState(isMasterEnabled: Boolean, dependentPreference: SwitchPreferenceCompat) {
            dependentPreference.isChecked = isMasterEnabled || dependentPreference.isChecked
            dependentPreference.isEnabled = !isMasterEnabled
        }
    }
}
