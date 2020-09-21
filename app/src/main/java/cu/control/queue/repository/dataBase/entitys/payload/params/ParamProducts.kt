package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep
import cu.control.queue.repository.dataBase.entitys.payload.Person

@Keep
class ParamProducts (
    val info: Map<String, String>,
    val products: String
): Param()