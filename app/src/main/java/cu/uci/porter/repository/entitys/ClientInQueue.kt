package cu.uci.porter.repository.entitys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ClientInQueue(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val queueId: Long,
    val clientId: Long,
    var lastRegistry: Long,
    var reIntent: Int = 0,
    var number: Int = 0
){
    companion object{
        const val TABLE_NAME = "ClientInQueue"
    }
}