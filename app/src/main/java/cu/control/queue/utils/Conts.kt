package cu.control.queue.utils

import android.annotation.SuppressLint
import android.os.Environment
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class Conts {
    companion object {
        val formatDateOnlyTime = SimpleDateFormat("h:mm:ss a")
        val formatDateBig = SimpleDateFormat("d 'de' MMMM 'del' yyyy")
        val formatDateBigNatural = SimpleDateFormat("d'/'M'/'yyyy")
        val formatDateMid = SimpleDateFormat("d 'de' MMMM")
        val APP_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/Porter@"

        //Preferences
        val DEFAULT_QUEUE_TIME_HOURS = 4
        val QUEUE_CANT = "QUEUE_CANT"
        val QUEUE_DAYS = "QUEUE_DAYS"
        val ALERTS = "alerts"
    }
}