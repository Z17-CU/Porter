package cu.control.queue.repository.dataBase.entitys.payload.params

import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import androidx.annotation.Keep

@Keep
class ParamDeleteMember (
    val person_id: Array<String>
): Param()