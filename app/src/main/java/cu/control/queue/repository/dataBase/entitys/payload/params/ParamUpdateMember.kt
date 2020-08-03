package cu.control.queue.repository.dataBase.entitys.payload.params

import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import androidx.annotation.Keep

@Keep
class ParamUpdateMember (
    val member_id: String,
    val info: String
): Param()