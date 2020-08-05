package cu.control.queue.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.Result
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Payload
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class Common {
    companion object {

        private val gson = Gson()

        fun getSex(idString: String?): Int? {
            return if (idString?.length == 11) {
                if (idString[9].toInt() % 2 == 0) {
                    Client.SEX_MAN
                } else {
                    Client.SEX_WOMAN
                }
            } else {
                null
            }
        }

        fun getAge(target: String, now: Calendar = GregorianCalendar()): Int {

            var age: Int

            require(target.length == 11) { "Invalid CI" }

            val month = target.substring(2, 4).toInt()
            require(month <= 12) { "Invalid CI" }

            val year = target.substring(0, 2).toInt() + getCentury(target)

            val day = target.substring(4, 6).toInt()

            val date1 =
                Date(GregorianCalendar(year, month - 1, day).timeInMillis) //fecha de nacimiento
            val dob = Calendar.getInstance()
            dob.time = date1

            require(!dob.after(now)) { "Can't be born in the future" }

            val year1 = now.get(Calendar.YEAR)
            val year2 = dob.get(Calendar.YEAR)

            age = year1 - year2

            val month1 = now.get(Calendar.MONTH)
            val month2 = dob.get(Calendar.MONTH)

            if (month2 > month1)
                age--
            else if (month1 == month2) {

                val day1 = now.get(Calendar.DAY_OF_MONTH)
                val day2 = dob.get(Calendar.DAY_OF_MONTH)

                if (day2 > day1)
                    age--

            }

            return age

        }

        private fun getCentury(ci: String): Int {

            return when (ci[6].toString().toInt()) {
                9 -> {
                    if (ci.substring(0, 2).toInt() == 0)
                        1900
                    else
                        1800
                }
                0, 1, 2, 3, 4, 5 -> {
                    if (ci.substring(0, 2).toInt() == 0)
                        2000
                    else
                        1900
                }
                else -> {
                    2000
                }
            }

        }

        fun stringToQueue(base64: String?): Queue? {
            if (base64.isNullOrBlank()) {
                return null
            }
            val string = Base64.decode(base64, Base64.DEFAULT)
            val data = String(string, StandardCharsets.UTF_8)

            val listType = object : TypeToken<Queue>() {

            }.type

            return gson.fromJson<Queue>(data, listType)
        }

        fun stringToListClient(base64: String?): List<Client>? {
            if (base64.isNullOrBlank()) {
                return null
            }
            val string = Base64.decode(base64, Base64.DEFAULT)
            val data = String(string, StandardCharsets.UTF_8)

            val listType = object : TypeToken<List<Client>?>() {

            }.type

            return gson.fromJson(data, listType)
        }

        fun porterHiToString(someObjects: PorterHistruct): String {
            return gson.toJson(someObjects)
        }

        fun payloadToString(someObjects: Payload): String {
            return gson.toJson(someObjects)
        }

        fun queueToString(someObjects: Queue): String {
            val text = gson.toJson(someObjects)
            val data = text.toByteArray(StandardCharsets.UTF_8)
            return Base64.encodeToString(data, Base64.DEFAULT)
        }

        fun clientListToString(someObjects: List<Client>): String {
            val text = gson.toJson(someObjects)
            val data = text.toByteArray(StandardCharsets.UTF_8)
            return Base64.encodeToString(data, Base64.DEFAULT)
        }

        fun selectQueueFromStorage(fragment: Fragment, requestCode: Int) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                this.type = "*/*"
            }

            fragment.startActivityForResult(intent, requestCode)
        }

        private fun share(context: Context, file: File, extension: String) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "application/$extension"
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            context.startActivity(share)
        }

        fun shareQueue(context: Context, file: File, extension: String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Compartir")
            builder.setMessage("¿Desea compartir el archivo de la cola?")
            builder.setNegativeButton("Cancelar", null)
            builder.setPositiveButton(
                "Compartir"
            ) { _, _ ->
                share(context, file, extension)
            }
            builder.setNeutralButton("Ver") { _, _ ->
                openFile(context, file, extension)
            }
            builder.create().show()
        }

        private fun openFile(context: Context, file: File, extension: String) {
            val path = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
            val pdfOpenintent = Intent(Intent.ACTION_VIEW)
            pdfOpenintent.flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
            pdfOpenintent.setDataAndType(path, "application/$extension")
            try {
                context.startActivity(pdfOpenintent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "No hay aplicaciones disponibles para abrir el fichero $extension.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }

        fun isValidCI(ci: CharSequence, context: Context): Boolean {

            val message = "Carné de identidad incorrecto"

            //limpiar lo que no sea numero
            val target = ci.replace(Regex("[^0123456789]"), "")

            //si se borro algo, ya es false
            if (target.length != ci.length)
                return false

            if (target.length != 11)
                return false

            val month = target.substring(2, 4).toInt()
            if (month < 1 || month > 12) {
                Toast.makeText(context, message, Toast.LENGTH_LONG)
                    .show()
                return false
            }

            var calendar = GregorianCalendar()
            var year = target.substring(0, 2).toInt() + 2000
            if (year >= calendar.get(Calendar.YEAR))
                year -= 1000

            val day = target.substring(4, 6).toInt()
            calendar = GregorianCalendar(year, month - 1, 1)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            if (day < 1 || day > daysInMonth) {
                Toast.makeText(context, message, Toast.LENGTH_LONG)
                    .show()
                return false
            }

            try {
                getAge(target, GregorianCalendar())
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, message, Toast.LENGTH_LONG)
                    .show()
                return false
            }

            return true

        }

        @SuppressLint("LogNotTimber")
        fun stringToClient(rawResult: Result, secure: String = ""): Client? {

            var client: Client? = null

            rawResult.text?.let {

                Log.d("stringToClient", it)

                val name = Regex("N:(.+?)*").find(it)?.value?.split(':')?.get(1)
                val lastName = Regex("A:(.+?)*").find(it)?.value?.split(':')?.get(1)
                val idString = Regex("CI:(.+?)*").find(it)?.value?.split(':')?.get(1)
                val id = idString?.toLong()
                val sex = getSex(idString)
                val fv = Regex("FV:(.+?)*").find(it)?.value?.split(':')?.get(1)

                Log.d("Regex result", " \n$name\n$lastName\n$id\n$fv ")

                if (name != null && lastName != null && id != null && fv != null && sex != null) {

                    client =
                        Client(
                            "$name $lastName",
                            Hash.getLongHash(idString, secure),
                            Hash.getMd5(idString, secure),
                            Hash.getMd5(fv, secure),
                            sex,
                            getAge(idString)
                        )
                }
            }

            return client
        }

        @SuppressLint("LogNotTimber")
        fun stringToPorterHistruct(rawResult: Result): PorterHistruct? {

            var porterHistruct: PorterHistruct? = null

            rawResult.text?.let {

                Log.d("stringToClient", it)

                val name = Regex("N:(.+?)*").find(it)?.value?.split(':')?.get(1)
                val lastName = Regex("A:(.+?)*").find(it)?.value?.split(':')?.get(1)
                val idString = Regex("CI:(.+?)*").find(it)?.value?.split(':')?.get(1)
                val fv = Regex("FV:(.+?)*").find(it)?.value?.split(':')?.get(1)

                if (name != null && lastName != null && idString != null && fv != null) {

                    porterHistruct =
                        PorterHistruct(
                            name,
                            lastName,
                            idString,
                            fv
                        )
                }
            }

            return porterHistruct
        }
    }
}