package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
class ParamCloseQueue(
    val info: Map<String, String>,
     val close_date: Long
) : Param()