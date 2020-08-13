package cu.control.queue.repository.dataBase.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cu.control.queue.repository.dataBase.entitys.payload.params.*

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun stringToActions(data: String): Map<String, Param> {

        val type = object : TypeToken<Map<String, ParamGeneral>>() {

        }.type

        val map = mutableMapOf<String, Param>()

        gson.fromJson<Map<String, ParamGeneral>>(data, type).map {

            when (it.key) {
                Param.TAG_CREATE_QUEUE -> {
                    val param = ParamCreateQueue(
                        it.value.store!!,
                        it.value.info!! as Map<String, String>,
                        it.value.created_date!!
                    )
                    map.put(it.key, param)
                }
                Param.TAG_UPDATE_QUEUE -> {
                    val param = ParamUpdateQueue(
                        it.value.info!! as Map<String, String>,
                        it.value.updated_date!!
                    )
                    map.put(it.key, param)
                }
                Param.TAG_DELETE_QUEUE -> {
                    val param = ParamDeleteQueue(
                        it.value.deleted_date!!
                    )
                    map.put(it.key, param)
                }
                Param.TAG_ADD_MEMBER -> {
                    val param = ParamAddMember(
                        it.value.person!!
                    )
                    map.put(it.key, param)
                }
                Param.TAG_UPDATE_MEMBER -> {
                    val param = ParamUpdateMember(
                        it.value.person!!
                    )
                    map.put(it.key, param)
                }
                Param.TAG_DELETE_MEMBER -> {
                    val param = ParamDeleteMember(
                        it.value.person!!
                    )
                    map.put(it.key, param)
                }
                else -> {

                }
            }
        }

        return map
    }

    @TypeConverter
    fun actionsToString(someObjects: Map<String, Param>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToArrayList(data: String): ArrayList<String> {

        val type = object : TypeToken<ArrayList<String>>() {

        }.type

        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun arraylistToString(someObjects: ArrayList<String>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMap(data: String): Map<String, Any> {

        val type = object : TypeToken<Map<String, Any>>() {

        }.type

        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun mapToString(someObjects: Map<String, Any>): String {
        return gson.toJson(someObjects)
    }
}