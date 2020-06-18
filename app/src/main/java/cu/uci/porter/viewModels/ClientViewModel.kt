package cu.uci.porter.viewModels

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cu.uci.porter.R
import cu.uci.porter.repository.ClientRepository
import cu.uci.porter.repository.entitys.Queue
import cu.uci.porter.utils.Common
import cu.uci.porter.utils.Conts
import cu.uci.porter.utils.Conts.Companion.APP_DIRECTORY
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    var allClientsInQueue = clientRepository.getAllClientInQueue()

    fun setAllClientsIDQueue(id: Long) {
        allClientsInQueue = clientRepository.getClientInQueue(id)
    }

    private val compositeDisposable = CompositeDisposable()

    var allQueues = clientRepository.getAllQueues()

    fun exportQueue(queue: Queue, context: Context) {
        val file = File(APP_DIRECTORY)
        if (!file.exists()) {
            file.mkdir()
        }

        try {
            val gpxfile = File(
                file,
                queue.name + " " + Conts.formatDateBig.format(queue.startDate) + " " + Calendar.getInstance()
                    .timeInMillis + ".cola"
            )
            val data = Common.queueToString(queue)
            val writer = FileWriter(gpxfile)
            writer.append(data)
            writer.flush()
            writer.close()
            (context as Activity).runOnUiThread {
                Toast.makeText(
                    context,
                    R.string.export_OK,
                    Toast.LENGTH_LONG
                ).show()
                Common.shareQueue(context, gpxfile, "cola")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun importQueue(fragment: Fragment, code: Int) {
        Common.selectQueueFromStorage(fragment, code)
    }

    fun saveImportQueue(queue: Queue) {

        Completable.create {

            if (queue.description == null) {
                queue.description = ""
            }
            queue.clientList?.map { client ->
                if (client.selected == null)
                    client.selected = false
                if (client.searched == null)
                    client.searched = false
            }

            clientRepository.saveCLients(queue.clientList ?: ArrayList())
            queue.clientsNumber = (queue.clientList ?: ArrayList()).size
            queue.clientList = ArrayList()
            clientRepository.saveCLientsInQueue(queue.clientInQueueList!!)
            queue.clientInQueueList = ArrayList()
            clientRepository.saveQueue(queue)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe().addTo(compositeDisposable)

    }

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}