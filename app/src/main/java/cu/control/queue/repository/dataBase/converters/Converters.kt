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
                        it.value.establishment!!,
                        it.value.info!!,
                        it.value.created_date!!
                    )
                    map.put(it.key, param)
                }
                Param.TAG_UPDATE_QUEUE -> {
                    val param = ParamUpdateQueue(
                        it.value.info!!,
                        it.value.update_date!!
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
                        it.value.info!!
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
}