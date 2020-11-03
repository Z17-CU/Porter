package cu.control.queue

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cu.control.queue.utils.Conts
import cu.control.queue.utils.Conts.Companion.DEFAULT_QUEUE_COUNT_VERIFY
import cu.control.queue.utils.Conts.Companion.DEFAULT_QUEUE_TIME_HOURS
import cu.control.queue.utils.IntCountEditTextPreference
import cu.control.queue.utils.IntEditTextPreference
import kotlinx.android.synthetic.main.settings_activity.*
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
        initToolBar()
    }

    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {
            setNavigationIcon(R.drawable.ic_back_custom)

            title = "Ajustes "
            setTitleTextColor(ContextCompat.getColor(context, R.color.blue_drawer))

            setNavigationOnClickListener {
                this@SettingsActivity.title = this@SettingsActivity.getString(R.string.app_name)
                (this@SettingsActivity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                onBackPressed()
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val isEnable =
                preferenceManager.findPreference<SwitchPreferenceCompat>("alerts")?.isChecked!!
            val isEnableCountDay =
                preferenceManager.findPreference<SwitchPreferenceCompat>("alerts_cant_day")?.isChecked!!

            preferenceManager.findPreference<IntEditTextPreference>("QUEUE_CANT")?.isEnabled =
                isEnable
            preferenceManager.findPreference<Preference>(QUERY_START_DATE)?.isEnabled =
                isEnable
            preferenceManager.findPreference<Preference>(QUERY_END_DATE)?.isEnabled =
                isEnable
            preferenceManager.findPreference<IntCountEditTextPreference>("QUEUE_CANT_DAY")?.isEnabled =
                isEnableCountDay
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

            if (preferenceManager.sharedPreferences.getInt(Conts.QUEUE_CANT, -1) == -1) {
                preferenceManager.sharedPreferences.edit()
                    .putInt(Conts.QUEUE_CANT, DEFAULT_QUEUE_TIME_HOURS).apply()
            }

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

            preferenceManager.findPreference<SwitchPreferenceCompat>("alerts_cant_day")
                ?.setOnPreferenceChangeListener { _, newValue ->

                    preferenceManager.findPreference<IntCountEditTextPreference>("QUEUE_CANT_DAY")?.isEnabled =
                        newValue as Boolean
                    true
                }

            val isEnableEmail =
                preferenceManager.findPreference<SwitchPreferenceCompat>("email")?.isChecked!!

            preferenceManager.findPreference<EditTextPreference>("emailAddress")?.isEnabled =
                isEnableEmail

            preferenceManager.findPreference<SwitchPreferenceCompat>("email")
                ?.setOnPreferenceChangeListener { _, newValue ->

                    preferenceManager.findPreference<EditTextPreference>("emailAddress")?.isEnabled =
                        newValue as Boolean

                    true
                }

            if (preferenceManager.sharedPreferences.getInt(Conts.QUEUE_CANT_DAY, -1) == -1) {
                preferenceManager.sharedPreferences.edit()
                    .putInt(Conts.QUEUE_CANT_DAY, DEFAULT_QUEUE_COUNT_VERIFY).apply()
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
            var calendar = Calendar.getInstance()
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

                    calendar = if (preferenceKey == QUERY_START_DATE) {
                        initDay(calendar)
                    } else {
                        endDay(calendar)
                    }

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

        private fun initDay(date: Calendar): Calendar {

            date.set(Calendar.HOUR_OF_DAY, 0)
            date.set(Calendar.MINUTE, 0)
            date.set(Calendar.SECOND, 0)
            date.set(Calendar.MILLISECOND, 0)

            return date
        }

        private fun endDay(date: Calendar): Calendar {

            date.set(Calendar.HOUR_OF_DAY, 23)
            date.set(Calendar.MINUTE, 59)
            date.set(Calendar.SECOND, 59)
            date.set(Calendar.MILLISECOND, 999)

            return date
        }
    }

    companion object {
        const val QUERY_START_DATE = "QUERY_START_DATE"
        const val QUERY_END_DATE = "QUERY_END_DATE"
    }
}