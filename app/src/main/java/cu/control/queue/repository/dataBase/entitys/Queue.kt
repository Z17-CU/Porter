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
    var description: String = "",
    var affiliation: String = "",
    var uuid: String?,
    val created_date: Long?,
    val updated_date: Long?,
    var collaborators: ArrayList<String> = ArrayList(),
    var downloaded: Boolean = true,
    var isSaved: Boolean = false,
    var owner: String,
    var isOffline: Boolean = false,
    var info: Map<String, Any>? = null,
    var store: String? = null,
    var province: String? = null,
    var municipality: String? = null,
    var storeName: String? = null,
    var alert: Boolean? = null,
    var textSeparator: String? = null,
    var checked: Boolean? = null
) {
    @Ignore
    var clientList: List<Client>? = ArrayList()

    @Ignore
    var clientInQueueList: List<ClientInQueue>? = ArrayList()

    companion object {
        const val TABLE_NAME = "Queue"
    }
}