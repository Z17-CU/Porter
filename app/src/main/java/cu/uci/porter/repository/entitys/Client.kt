package cu.uci.porter.repository.entitys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Client(
    var name: String,
    var lastName: String,
    @PrimaryKey
    var id: Long,
    var ci: String,
    var fv: String?,
    var sex: Int?,
    var age: Int,
    var lastRegistry: Long,
    val queueId: Int,
    var reIntent: Int = 0
) {
    companion object {
        const val TABLE_NAME = "Client"
        const val SEX_WOMAN = 1
        const val SEX_MAN = 2
    }
}