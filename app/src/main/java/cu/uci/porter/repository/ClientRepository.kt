package cu.uci.porter.repository

import androidx.lifecycle.LiveData
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val dao: Dao
) {

    fun getAllClients(id: Int): LiveData<List<Client>> {

        return dao.getAllClients(id)
    }
    fun getAllQueues(): LiveData<List<Queue>> {

        return dao.getAllQueues()
    }
}