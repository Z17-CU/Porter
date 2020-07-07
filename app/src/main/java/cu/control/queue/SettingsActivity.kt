package cu.control.queue

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cu.control.queue.utils.Conts
import cu.control.queue.utils.IntEditTextPreference
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val isEnable =
                preferenceManager.findPreference<SwitchPreferenceCompat>("alerts")?.isChecked!!

            preferenceManager.findPreference<IntEditTextPreference>("QUEUE_CANT")?.isEnabled =
                isEnable
            preferenceManager.findPreference<Preference>(QUERY_START_DATE)?.isEnabled =
                isEnable
            preferenceManager.findPreference<Preference>(QUERY_END_DATE)?.isEnabled =
                isEnable

            preferenceManager.findPreference<Preference>(QUERY_START_DATE)?.summary =
                Conts.formatDateBig.format(
                    preferenceManager.sharedPreferences.getLong(
                        QUERY_START_DATE,
                        Calendar.getInstance().timeInMillis
                    )
                )
            preferenceManager.findPreference<Preference>(QUERY_END_DATE)?.summary =
                Conts.formatDateBig.format(
                    preferenceManager.sharedPreferences.getLong(
                        QUERY_END_DATE,
                        Calendar.getInstance().timeInMillis
                    )
                )


            preferenceManager.findPreference<SwitchPreferenceCompat>("alerts")
                ?.setOnPreferenceChangeListener { _, newValue ->

                    preferenceManager.findPreference<IntEditTextPreference>("QUEUE_CANT")?.isEnabled =
                        newValue as Boolean
                    preferenceManager.findPreference<Preference>(QUERY_START_DATE)?.isEnabled =
                        newValue
                    preferenceManager.findPreference<Preference>(QUERY_END_DATE)?.isEnabled =
                        newValue

                    true
                }

            preferenceManager.findPreference<Preference>(QUERY_START_DATE)
                ?.setOnPreferenceClickListener {
                    showDatePicker(QUERY_START_DATE)
                    true
                }
            preferenceManager.findPreference<Preference>(QUERY_END_DATE)
                ?.setOnPreferenceClickListener {
                    showDatePicker(QUERY_END_DATE)
                    true
                }

        }

        private fun showDatePicker(preferenceKey: String) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = preferenceManager.sharedPreferences.getLong(
                preferenceKey,
                Calendar.getInstance().timeInMillis
            )

            val dpd = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, month, day ->

                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    preferenceManager.sharedPreferences.edit()
                        .putLong(preferenceKey, calendar.timeInMillis).apply()
                    preferenceManager.findPreference<Preference>(preferenceKey)?.summary =
                        Conts.formatDateBig.format(calendar.timeInMillis)

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()

        }
    }

    companion object {
        const val QUERY_START_DATE = "QUERY_START_DATE"
        const val QUERY_END_DATE = "QUERY_END_DATE"
    }
}