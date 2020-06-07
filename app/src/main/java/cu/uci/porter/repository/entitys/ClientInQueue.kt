package cu.uci.porter.repository.entitys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ClientInQueue(
    @PrimaryKey
    var id: Long,
    val queueId: Long,
    var clientId: Long,
    var lastRegistry: Long,
    var reIntent: Int = 0,
    var number: Int = 0,
    var isChecked: Boolean = false
){
    companion object{
        const val TABLE_NAME = "ClientInQueue"
    }
}