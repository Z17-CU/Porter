package cu.uci.porter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cu.uci.porter.utils.IntEditTextPreference

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            preferenceManager.findPreference<IntEditTextPreference>("QUEUE_CANT")?.isEnabled =
                preferenceManager.findPreference<SwitchPreferenceCompat>("alerts")?.isChecked!!
            preferenceManager.findPreference<IntEditTextPreference>("QUEUE_DAYS")?.isEnabled =
                preferenceManager.findPreference<SwitchPreferenceCompat>("alerts")?.isChecked!!

            preferenceManager.findPreference<SwitchPreferenceCompat>("alerts")
                ?.setOnPreferenceChangeListener { _, newValue ->

                    preferenceManager.findPreference<IntEditTextPreference>("QUEUE_CANT")?.isEnabled =
                        newValue as Boolean
                    preferenceManager.findPreference<IntEditTextPreference>("QUEUE_DAYS")?.isEnabled =
                        newValue

                    true
                }

        }
    }
}