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

    fun getAllClients(id: Long): LiveData<List<Client>> {

        return dao.getAllClients(id)
    }

    fun getAllQueues(): LiveData<List<Queue>> {

        return dao.getAllQueues()
    }

    fun getAllClientsInRangue(id: Long, min: Int, max: Int): LiveData<List<Client>> {
        return dao.getAllClientsInRangue(id, min, max)
    }
}