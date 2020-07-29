package cu.control.queue.repository.dataBase.entitys

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Queue(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var name: String,
    var startDate: Long,
    var clientsNumber: Int = 0,
    var description: String = ""
) {
    @Ignore
    var clientList: List<Client>? = ArrayList()
    @Ignore
    var clientInQueueList: List<ClientInQueue>? = ArrayList()
    companion object {
        const val TABLE_NAME = "Queue"
    }
}