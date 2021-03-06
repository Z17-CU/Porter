package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
class ParamCreateQueue(
    val store: String,
    val info: Map<String, Any>,
    val created_date: Long
) : Param()