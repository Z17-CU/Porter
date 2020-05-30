package cu.uci.porter.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val dao: Dao
) {

    fun getAllQueues() = dao.getAllQueues()

    fun saveCLients(clients: List<Client>) = dao.insertClient(clients)

    fun saveQueue(queue: Queue) = dao.insertQueue(queue)

    fun getAllClientInQueue() = dao.getClientsInQueue()

    fun getClientInQueueList(id: Long) = dao.getClientsInQueueList(id)
}