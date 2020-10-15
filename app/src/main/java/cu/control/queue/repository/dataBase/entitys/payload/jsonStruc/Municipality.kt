package cu.control.queue.repository.dataBase.entitys.payload.jsonStruc

data class Municipality(
    val id: String,
    val name: String,
    val store: List<Store>
)