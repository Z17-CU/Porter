package cu.uci.porter.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cu.uci.porter.repository.ClientRepository
import javax.inject.Inject

class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    fun allClient(id: Int) = clientRepository.getAllClients(id)
    var allQueues = clientRepository.getAllQueues()

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}