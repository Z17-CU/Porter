package cu.uci.porter.utils

import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.Queue
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
    }
}