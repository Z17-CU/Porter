package cu.uci.porter.repository.entitys

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Queue(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var name: String,
    var startDate: Long,
    var clientsNumber: Int = 0
) {
    @Ignore
    var clientList: List<Client>? = ArrayList()
    companion object {
        const val TABLE_NAME = "Queue"
    }
}