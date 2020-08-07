package cu.control.queue.repository.dataBase.entitys.payload

class Person(
    val ci: String,
    val fv: String,
    val info: Map<String, String>
){
    companion object{
        const val KEY_ADD_DATE = "add_date"
        const val KEY_REINTENT_COUNT = "reIntent"
        const val KEY_NUMBER = "number"

        const val KEY_MEMBER_UPDATED_DATE = "updated_date"
        const val KEY_CHECKED = "checked"
        const val KEY_UNCHECKED = "unchecked"

        const val MODE_CHECK = "MODE_CHECK"
        const val MODE_UNCHECK = "MODE_UNCHECK"
        const val MODE_INCREMENT_REINTENT = "MODE_INCREMENT_REINTENT"
    }
}