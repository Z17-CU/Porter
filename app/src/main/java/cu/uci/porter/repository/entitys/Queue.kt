package cu.uci.porter.repository.entitys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Queue(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var name: String,
    var startDate: Long,
    var clientsNumber: Int = 0
) {
    companion object {
        const val TABLE_NAME = "Queue"
    }
}