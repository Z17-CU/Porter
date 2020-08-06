package cu.control.queue.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.*

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

    fun setLastName(lastName: String) = editor.putString(LATS_NAME, lastName).commit()

    fun setCI(ci: String) = editor.putString(CI, ci).commit()

    fun setFV(fv: String) = editor.putString(FV, fv).commit()

    fun setFirstRun() {
        editor.putBoolean(FIRST_TIME, false).commit()
        editor.commit()
    }

    fun getSecureHasCode() = preferences.getString(HASH_SECURE, "") ?: ""

    fun setSecureHasCode() {

        //crear una nueva clave aleatoria para el hash
        val uuid = UUID.randomUUID()

        //eliminar los guiones del UUID
        val string = uuid.toString().replace("-", "")

        //tomar solo los primeros 20 caracteres
        val hash = if (string.length > 20)
            string.substring(0, 20)
        else string

        /**
         * Si se activa el hash random, las funciones de exportar/importar no funcionaran
         * Ventaja: Mayor seguridad
         */
//        editor.putString(HASH_SECURE, hash).commit()

        /**
         * De esta forma funcionan las opciones de exportar/importar
         * Desventaja: Menor seguridad
         */
        editor.putString(HASH_SECURE, "a937ff5df73b44aa9b86").commit()
    }

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREFERENCE_CONFIGURATION_NAME = "configuration"
        private const val FIRST_TIME = "isFirstRun2"
        private const val HASH_SECURE = "HASH_SECURE"
        private const val NAME = "NAME"
        private const val LATS_NAME = "LATS_NAME"
        private const val CI = "CI"
        private const val FV = "FV"
    }
}