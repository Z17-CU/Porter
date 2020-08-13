package cu.control.queue.repository.dataBase.entitys.payload

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
open class Person(
    @PrimaryKey
    val ci: String,
    val fv: String,
    val info: Map<String, Any>
){
    companion object{
        const val KEY_ADD_DATE = "added_date"
        const val KEY_DELETE_DATE = "deleted_date"
        const val KEY_REINTENT_COUNT = "attempts"
        const val KEY_NUMBER = "number"
        const val KEY_NAME = "name"
        const val KEY_LAST_NAME = "last_name"

        const val KEY_MEMBER_UPDATED_DATE = "updated_date"
        const val KEY_CHECKED = "checked_date"
        const val KEY_UNCHECKED = "unchecked_date"

        const val MODE_CHECK = "MODE_CHECK"
        const val MODE_UNCHECK = "MODE_UNCHECK"
        const val MODE_INCREMENT_REINTENT = "MODE_INCREMENT_REINTENT"

        const val TABLE_NAME = "Person"
    }
}