package cu.control.queue.repository.dataBase.entitys.payload

class QueueMember (
    val queue_uuid: String,
    val ci: String,
    val fv: String,
    val affiliation: Int,
    val serial: String,
    val info: String
)