package cu.control.queue.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.dataBase.entitys.payload.jsonStruc.jsonStrucItem
 import java.io.*


class JsonWrite(val context: Context) {

    fun writeToFile(data: String) {


        val userString: String = data // Define the File Path and its Name

        val file = File(context.filesDir, "stores.json")
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(userString)
        bufferedWriter.close()
    }

      fun readFromFile(): String? {
        var ret = ""
        var inputStream: InputStream? = null
        try {
            inputStream = context.openFileInput("stores.json")
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = java.lang.StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append(receiveString)
                }
                ret = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        } finally {
            try {
                inputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ret
    }



}