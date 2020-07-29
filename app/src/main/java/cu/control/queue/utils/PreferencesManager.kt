package cu.control.queue.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(PREFERENCE_CONFIGURATION_NAME, PRIVATE_MODE)
        editor = preferences.edit()
    }

    fun isFirstRun() = preferences.getBoolean(FIRST_TIME, true)

    fun setName(name: String) = editor.putString(NAME, name).commit()

    fun setLastName(lastName: String) = editor.putString(LATS_NAME, lastName).commit()

    fun setCI(ci: String) = editor.putString(CI, ci).commit()

    fun setFV(fv: String) = editor.putString(CI, fv).commit()

    fun setFirstRun() {
        editor.putBoolean(FIRST_TIME, false).commit()
        editor.commit()
    }

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREFERENCE_CONFIGURATION_NAME = "configuration"
        private const val FIRST_TIME = "isFirstRun"
        private const val NAME = "NAME"
        private const val LATS_NAME = "LATS_NAME"
        private const val CI = "CI"
        private const val FV = "FV"
    }
}