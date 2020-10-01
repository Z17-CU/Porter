package cu.control.queue.viewModels

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.interfaces.OnAction
import cu.control.queue.repository.ClientRepository
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Payload
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import cu.control.queue.utils.Conts
import cu.control.queue.utils.Conts.Companion.APP_DIRECTORY
import cu.control.queue.utils.PreferencesManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val context: Context
) : ViewModel(), OnAction {

    var allClientsInQueue = clientRepository.getAllClientInQueue()

    var creatingQueue = MutableLiveData<Queue?>().default(null)

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

    override fun onRegistreAction(
        queueId: String,
        param: Param,
        paramTag: String,
        context: Context
    ) {
        Completable.create {

            val queue = clientRepository.getQueue(queueId)

            if (queue == null || queue.isOffline) {
                it.onComplete()
            } else {
                queue.let { queue ->
                    queue.isSaved = false
                    clientRepository.saveQueue(queue)
                }

                var payload = clientRepository.getPayload(queueId)

                if (payload == null) {
                    val map = mutableMapOf<String, Param>()
                    map[paramTag] = param
                    payload = Payload(PreferencesManager(context).getId(), queueId, map)
                } else {
                    (payload.methods as HashMap)[paramTag] = param
                }

                clientRepository.insertPayload(payload)
                it.onComplete()
            }
        }.observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe().addTo(compositeDisposable)
    }

    fun observePayloads(lifecycleOwner: LifecycleOwner) {
        clientRepository.getAllPayloads().observe(lifecycleOwner, androidx.lifecycle.Observer {
            sendPayloads(it)
        })
    }

    fun sendPayloads(payloads: List<Payload>) {
        Single.create<Pair<Int, String>> {

            val payloadToDelete: ArrayList<Payload> = ArrayList()

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            var result: Response<String>? = null
            payloads.map { payload ->
                val tempResult = APIService.apiService.sendActions(
                    headers = headerMap,
                    payload = Common.payloadToString(payload)
                ).execute()
                if (result == null || tempResult.code() != 200) {
                    result = tempResult
                }
                if (tempResult.code() == 200) {
                    payloadToDelete.add(payload)
                    clientRepository.getQueue(payload.queue_uuid)?.let { queue ->
                        queue.isSaved = true
                        clientRepository.saveQueue(queue)

                    }
                }
            }

            clientRepository.deletePayloads(payloadToDelete)

            it.onSuccess(Pair(result?.code() ?: -1, result?.message() ?: ""))
        }.subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .subscribe { pair, error ->
                if (error != null) {
                    showToast(error.message ?: "Null error")
                } else if (pair.first != 200 && pair.second.isNotEmpty()) {
                    showToast(pair.second)
                }
            }.addTo(compositeDisposable)
    }

    private fun showToast(text: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}