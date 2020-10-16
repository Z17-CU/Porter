package cu.control.queue.utils

import android.annotation.SuppressLint
import android.os.Environment
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class Conts {
    companion object {
        val formatDateOnlyTime = SimpleDateFormat("h:mm:ss a")
        val formatDateOnlyTimeNoSecond = SimpleDateFormat("h:mm a")
        val formatDateBig = SimpleDateFormat("d 'de' MMMM 'del' yyyy")
        val formatDateBigNatural = SimpleDateFormat("d'/'M'/'yyyy")
        val formatDateMid = SimpleDateFormat("d 'de' MMMM")
        val APP_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/Porter@"

        //Preferences
        const val DEFAULT_QUEUE_TIME_HOURS = 4
        const val DEFAULT_QUEUE_COUNT_VERIFY = 3
        const val QUEUE_CANT = "QUEUE_CANT"
        const val QUEUE_CANT_DAY = "QUEUE_CANT_DAY"
        const val QUEUE_DAYS = "QUEUE_DAYS"

        const val ALERTS = "alerts"
    }
}