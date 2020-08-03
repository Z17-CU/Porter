package cu.control.queue.repository.dataBase.entitys.payload

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import cu.control.queue.repository.dataBase.entitys.payload.params.Param

@Keep
@Entity
class Payload(
    val user: String,
    @PrimaryKey
    val queue_uuid: String,
    val methods: Map<String, Param>
){
    companion object{
        const val TABLE_NAME = "Payload"
    }
}