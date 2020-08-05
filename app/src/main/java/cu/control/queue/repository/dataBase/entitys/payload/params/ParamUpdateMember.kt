package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
class ParamUpdateMember (
    val member_id: String,
    val info: Map<String, String>
): Param()