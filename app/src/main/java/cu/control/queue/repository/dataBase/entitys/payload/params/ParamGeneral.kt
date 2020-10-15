package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep
import cu.control.queue.repository.dataBase.entitys.payload.Person

@Keep
open class ParamGeneral(
    val person: ArrayList<Person>? = null,
    val store: String? = null,
    val info: Map<String, Any>? = null,
    val created_date: Long? = null,
    val deleted_date: Long? = null,
    val updated_date: Long? = null,
    val close_date: Long? = null
) : Param()