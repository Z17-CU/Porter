package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
class ParamDeleteQueue(
    val deleted_date: Long
) : Param()