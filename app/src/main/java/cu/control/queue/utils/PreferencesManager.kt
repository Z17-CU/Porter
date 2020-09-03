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

    fun getCi() = preferences.getString(CI, "Null_CI") ?: ""

    fun getName() = preferences.getString(NAME, "Null_NAME") ?: ""

    fun getLastName() = preferences.getString(LATS_NAME, "Null_LAST_NAME") ?: ""

    fun getFv() = preferences.getString(FV, "Null_FV") ?: ""


    fun getId() = getCi() + "." + getFv()

    fun getStoreVersion() = preferences.getInt(STORE_VERSION, 1)
    fun getLastInfoCreateQueue() = preferences.getString(LAST_INFO_CREATE_QUEUE, "")
    fun setLastName(lastName: String) = editor.putString(LATS_NAME, lastName).commit()

    fun setCI(ci: String) = editor.putString(CI, ci).commit()

    fun setFV(fv: String) = editor.putString(FV, fv).commit()

    fun setStoreVersion(store_version: Int) = editor.putInt(STORE_VERSION, store_version).commit()
    fun setLastInfoCreateQueue(idProvince: Int, idMunicipie: Int, idStore: Int) {
        val dataStorage = "$idProvince,$idMunicipie,$idStore"
        editor.putString(LAST_INFO_CREATE_QUEUE, dataStorage).commit()

    }


    fun setFirstRun() {
        editor.putBoolean(FIRST_TIME, false).commit()
        editor.commit()
    }

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREFERENCE_CONFIGURATION_NAME = "configuration"
        private const val FIRST_TIME = "isFirstRun2"
        private const val NAME = "NAME"
        private const val LATS_NAME = "LATS_NAME"
        private const val CI = "CI"
        private const val FV = "FV"
        private const val STORE_VERSION = "STORE_VERSION"
        private const val STORE = "STORE"
        private const val LAST_INFO_CREATE_QUEUE = "LAST_INFO_CREATE_QUEUE"
    }
}