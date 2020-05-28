package cu.uci.porter.utils

import android.annotation.SuppressLint
import android.os.Environment
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class Conts {
    companion object {
        val formatDateOnlyTime = SimpleDateFormat("h:mm:ss a")
        val formatDateBig = SimpleDateFormat("d 'de' MMMM 'del' yyyy h:mm a")
        val APP_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/Porter@"
    }
}