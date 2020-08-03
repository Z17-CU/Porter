package cu.control.queue.repository.dataBase.entitys.payload.params

import cu.control.queue.repository.dataBase.entitys.payload.Person
import androidx.annotation.Keep

@Keep
class ParamAddMember (
    val person: Array<Person>
): Param()