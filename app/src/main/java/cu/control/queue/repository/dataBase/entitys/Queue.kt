package cu.control.queue.repository.dataBase.entitys

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import cu.control.queue.repository.dataBase.entitys.payload.Person

@Entity
class Queue(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var name: String,
//    var products:String,
    var startDate: Long,
    var clientsNumber: Int = 0,
    var description: String = "",
    var uuid: String?,
    val province: String?,
    val municipality: String?,
    var business: String?,
    val created_date: Long?,
    val updated_date: Long?,
    var collaborators: ArrayList<String> = ArrayList(),
    var downloaded: Boolean = true,
    var isSaved: Boolean = false,
    var owner: String,
    var isOffline: Boolean = false
) {
    @Ignore
    var clientList: List<Client>? = ArrayList()
    @Ignore
    var clientInQueueList: List<ClientInQueue>? = ArrayList()
    companion object {
        const val TABLE_NAME = "Queue"
    }
}