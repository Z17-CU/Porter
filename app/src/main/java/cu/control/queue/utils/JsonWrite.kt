package cu.control.queue.utils

import android.content.Context
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

//     fun readFromFile(): List<jsonStrucItem>? {
//        val file = File(context.filesDir, "stores.json")
//        val fileReader = FileReader(file)
//        val bufferedReader = BufferedReader(fileReader)
//        val stringBuilder = StringBuilder()
//        var line = bufferedReader.readLine()
//        while (line != null) {
//            stringBuilder.append(line).append("\n")
//            line = bufferedReader.readLine()
//        }
//        bufferedReader.close() // This responce will have Json Format String
//         val gson: Gson = GsonBuilder().create()
//         val porterHistruct: PorterHistruct = gson.fromJson(stringBuilder.toString(), PorterHistruct::class.java)
//        val response =porterHistruct.stores
//        return response
//    }
}