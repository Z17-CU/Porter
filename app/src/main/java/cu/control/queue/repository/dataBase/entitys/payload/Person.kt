package cu.control.queue.repository.dataBase.entitys.payload

class Person(
    val ci: String,
    val fv: String,
    val info: Map<String, Long>
){
    companion object{
        const val KEY_ADD_DATE = "added_date"
        const val KEY_DELETE_DATE = "deleted_date"
        const val KEY_REINTENT_COUNT = "attempts"
        const val KEY_NUMBER = "number"

        const val KEY_MEMBER_UPDATED_DATE = "updated_date"
        const val KEY_CHECKED = "checked_date"
        const val KEY_UNCHECKED = "unchecked_date"

        const val MODE_CHECK = "MODE_CHECK"
        const val MODE_UNCHECK = "MODE_UNCHECK"
        const val MODE_INCREMENT_REINTENT = "MODE_INCREMENT_REINTENT"
    }
}