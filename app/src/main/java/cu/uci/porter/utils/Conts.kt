package cu.uci.porter.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class Conts {
    companion object {
        val formatDateOnlyTime = SimpleDateFormat("h:mm:ss a")
        val formatDateBig = SimpleDateFormat("d 'de' MMM 'del' yy h:mm a")
    }
}