package cu.uci.porter.repository

import androidx.lifecycle.LiveData
import cu.uci.porter.repository.entitys.Client
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val dao: Dao
) {

    fun getAllClients(): LiveData<List<Client>> {

        return dao.getAllClients()
    }
}