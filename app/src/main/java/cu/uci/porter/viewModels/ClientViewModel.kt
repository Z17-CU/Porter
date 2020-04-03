package cu.uci.porter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cu.uci.porter.repository.ClientRepository
import cu.uci.porter.repository.entitys.Client
import javax.inject.Inject

class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    var allClients = clientRepository.getAllClients(-1)

    fun setAllClient(id: Int) {
        allClients = clientRepository.getAllClients(id)
    }

    fun setAllClientsInRangue(id: Int, min: Int, max: Int) {
        allClients = clientRepository.getAllClientsInRangue(id, min, max)
    }

    var allQueues = clientRepository.getAllQueues()

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}