package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
class ParamUpdateQueue (
    val info: Map<String, Any>,
    val updated_date: Long
): Param()