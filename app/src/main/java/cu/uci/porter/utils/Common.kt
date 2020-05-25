package cu.uci.porter.utils

import android.util.Log
import cu.uci.porter.repository.entitys.Client
import java.util.*

class Common {
    companion object {

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
    }
}