package cu.control.queue.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.ClientRepository

class ClientViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <ClientViewModel : ViewModel?> create(modelClass: Class<ClientViewModel>): ClientViewModel {

        return ClientViewModel(
            ClientRepository(
                AppDataBase.getInstance(context).dao()
            )
        ) as ClientViewModel
    }
}