package cu.control.queue.repository.dataBase.entitys.payload

import cu.control.queue.repository.dataBase.entitys.Product

class Queue(
    val uuid: String,
    val store: String,
    val members: ArrayList<Person>?,
    val operators: ArrayList<Person>?,
    val created_date: Long,
    val updated_date: Long,
    val finished_date: Long,
    val deleted_date: Long,
    val products: ArrayList<String>?,
    val info: Map<String, Any>?
)