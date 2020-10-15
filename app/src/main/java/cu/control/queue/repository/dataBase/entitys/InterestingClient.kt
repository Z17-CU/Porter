package cu.control.queue.repository.dataBase.entitys

class InterestingClient(
    val number: String,
    val storeName: String,
    val day: String,
    val hour: String,
    val queueName: String,
    val products: ArrayList<String>
)