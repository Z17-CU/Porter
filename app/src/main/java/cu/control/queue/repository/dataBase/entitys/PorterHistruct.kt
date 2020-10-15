package cu.control.queue.repository.dataBase.entitys

import androidx.annotation.Keep
import cu.control.queue.repository.dataBase.entitys.payload.jsonStruc.jsonStrucItem

@Keep
class PorterHistruct(
    val name: String,
    val last_name: String,
    val ci: String,
    val fv: String,
    val store_version: Int,
    val stores: List<jsonStrucItem>? = null
)