package cu.control.queue.utils

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
import cu.control.queue.repository.entitys.Client
import cu.control.queue.repository.entitys.Queue
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

        fun queueToString(someObjects: Queue): String {
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
    }
}