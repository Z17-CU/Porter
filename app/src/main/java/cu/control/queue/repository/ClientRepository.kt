package cu.control.queue.repository

import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Payload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val dao: Dao
) {

    fun getAllQueues() = dao.getAllQueues()

    fun saveCLients(clients: List<Client>) = dao.insertClient(clients)

    fun saveCLientsInQueue(clientsInQueue: List<ClientInQueue>) = dao.insertClientInQueue(clientsInQueue)

    fun saveQueue(queue: Queue) = dao.insertQueue(queue)

    fun getAllClientInQueue() = dao.getClientsInQueue()

    fun getClientInQueue(id: Long) = dao.getClientsInQueue(id)

    fun getPayload(id: String) = dao.getPayload(id)

    fun insertPayload(payload: Payload) = dao.insertPayload(payload)

    fun getAllPayloads() = dao.getAllPayloadLive()

    fun deletePayloads(payloads: ArrayList<Payload>) = dao.deletePayload(payloads)
}