package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
class ParamDeleteMember (
    val info: Map<String, String>
): Param()