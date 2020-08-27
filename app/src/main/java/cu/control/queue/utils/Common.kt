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
import cu.control.queue.repository.dataBase.entitys.payload.Hi403Message
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

        fun showHiErrorMessage(context: Context, message: String): AlertDialog {

            val type = object : TypeToken<Hi403Message>() {

            }.type

            val hi403Message = Gson().fromJson<Hi403Message>(message, type)

            val dialog = AlertDialog.Builder(context)
                .setTitle(hi403Message.title)
                .setMessage(hi403Message.message)

            var count = 0
            hi403Message.url.map {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.value))
                when (count) {
                    0 -> {
                        dialog.setPositiveButton(it.key) { _, _ ->
                            context.startActivity(intent)
                        }
                    }
                    1 -> {
                        dialog.setNegativeButton(it.key) { _, _ ->
                            context.startActivity(intent)
                        }
                    }
                    2 -> {
                        dialog.setNeutralButton(it.key) { _, _ ->
                            context.startActivity(intent)
                        }
                    }
                    else -> {

                    }
                }
                count++
            }

            return dialog.create()
        }

        fun getAge(idString: String): Int {
            val currentYearBig = Calendar.getInstance().get(Calendar.YEAR)
            val currentYear = currentYearBig.toString().substring(2, 4).toInt()
            var clientYear = idString.substring(0, 2).toInt()

            clientYear = if (currentYear < clientYear) {

                if (clientYear < 10) {
                    "190$clientYear".toInt()
                } else {
                    "19$clientYear".toInt()
                }
            } else {
                if (clientYear < 10) {
                    "200$clientYear".toInt()
                } else {
                    "20$clientYear".toInt()
                }
            }

            Log.d("Ages", "$currentYearBig $clientYear")
            return currentYearBig - clientYear
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

        fun isValidCI(ci: String, context: Context): Boolean {

            if (ci.length == 11) {
                val mount = ci.substring(2, 4).toInt()
                val day = ci.substring(4, 6).toInt()
                val isValid = mount in 1..12 && day in 1..31
                if (!isValid) {
                    Toast.makeText(context, "Carné de identidad incorrecto", Toast.LENGTH_LONG)
                        .show()
                }
                return isValid
            }

            return false
        }

//        fun isValidFV(fv: String, context: Context): Boolean {
//            var numbers = "0123456789"
//            for (x in fv.indices) {
//                val c: Char = fv.charAt(x)
//                // Si no está entre a y z, ni entre A y Z, ni es un espacio
//                if (!(c in 'a'..'z' || c in 'A'..'Z' || c == ' ')) {
//                    return false
//                }
//            }
//            return true
//
//        }

        @SuppressLint("LogNotTimber")
        fun stringToClient(rawResult: Result): Client? {

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
                            id,
                            idString,
                            fv,
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