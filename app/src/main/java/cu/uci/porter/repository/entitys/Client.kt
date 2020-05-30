package cu.uci.porter.repository.entitys

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import cu.uci.porter.repository.entitys.Client.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
class Client(
    var name: String,
    @PrimaryKey
    var id: Long,
    var ci: String,
    var fv: String?,
    var sex: Int?,
    var age: Int,
    var lastRegistry: Long = 0,
    var reIntent: Int = 0,
    var number: Int = 0
) {
    companion object {
        const val TABLE_NAME = "Client"
        const val SEX_WOMAN = 1
        const val SEX_MAN = 2
    }
}