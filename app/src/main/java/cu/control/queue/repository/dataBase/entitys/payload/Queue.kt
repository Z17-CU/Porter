package cu.control.queue.repository.dataBase.entitys.payload

class Queue(
    val uuid: String,
    val store: Int,
    val members: ArrayList<Person>?,
    val operators: ArrayList<Person>?,
    val created_date: Long,
    val updated_date: Long,
    val finished_date: Long,
    val deleted_date: Long,
    val info: Any?
)