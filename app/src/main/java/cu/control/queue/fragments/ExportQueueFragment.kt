package cu.control.queue.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.R
import cu.control.queue.adapters.AdapterExportQueues
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.utils.CSVWriter
import cu.control.queue.utils.Conts
import cu.control.queue.viewModels.ClientViewModel
import cu.control.queue.viewModels.ClientViewModelFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.export_queue.*
import kotlinx.android.synthetic.main.room_queues.toolbar
import me.yokeyword.fragmentation.SupportFragment
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExportQueueFragment() : SupportFragment(), onClickListener {

    private lateinit var dao: Dao
    private val compositeDisposable = CompositeDisposable()
    private lateinit var adapterSave: AdapterExportQueues
    private lateinit var viewModel: ClientViewModel
    var contentList: List<Client> = ArrayList()
    lateinit var queue: Queue

    private var exportFrom = 0
    private var exportTo = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.export_queue, null)

        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        dao = AppDataBase.getInstance(view.context).dao()
        val tempViewModel: ClientViewModel by viewModels(
            factoryProducer = { ClientViewModelFactory(view.context) }
        )
        viewModel = tempViewModel

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()
        _recyclerViewExportQueue.layoutManager = LinearLayoutManager(view.context)

        adapterSave = AdapterExportQueues(this)
        _recyclerViewExportQueue.adapter = adapterSave

//        add_colaborator.setOnClickListener {
//            DialogAddCollaborator(requireContext()).create().show()
//        }

        viewModel.allQueues.observe(viewLifecycleOwner, Observer {

            refreshAdapter()
        })
        export_queue.setOnClickListener {
            val exportList = adapterSave.exportList
            if (exportList.size > 0) {
                Toast.makeText(context,"Exportando ${exportList.size} cola(s), por favor espere...",Toast.LENGTH_SHORT).show()
                exportList.map { queue ->
                    this.queue = queue
                    Single.create<List<ClientInQueue>> {
                        val clientsInQueue = dao.getClientsInQueueList(queue.id!!)
                        it.onSuccess(clientsInQueue)
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { clientsInQueue, _ ->
                            clientsInQueue.map {

                                Single.create<List<Client>> { singleEmitter ->

                                    var idList: List<Long> = ArrayList()
                                    clientsInQueue.map {
                                        idList = idList + it.clientId
                                        Timber.d(it.clientId.toString())
                                    }
                                    var clientList: List<Client> = ArrayList()

                                    clientList = dao.getClients(idList)


                                    clientList.map { client ->
                                        val thisClientInQueue =
                                            clientsInQueue.find { it.clientId == client.id }!!
                                        client.lastRegistry = thisClientInQueue.lastRegistry
                                        client.reIntent = thisClientInQueue.reIntent
                                        client.number = thisClientInQueue.number
                                        client.isChecked = thisClientInQueue.isChecked
                                        client.repeatedClient = thisClientInQueue.repeatedClient
                                    }

                                    clientList = clientList.sortedBy { it.number }

                                    singleEmitter.onSuccess(clientList)
                                }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { clients, _ ->
                                        contentList = clients

                                    }
                            }

                        }.addTo(compositeDisposable)

                }
                exportQueueCSV(contentList)
                Timber.e(exportList.size.toString())
            } else {
                Toast.makeText(
                    context,
                    requireActivity().getString(R.string.export_list),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun exportQueueCSV(it: List<Client>) {

        if (it.isNotEmpty()) {
            val list = it.subList(exportFrom, it.size)
            val timeFormat = SimpleDateFormat("h:mm a", Locale("es", "CU"))
            val dateFormat = SimpleDateFormat("d 'de' MMM 'del' yyyy", Locale("es", "CU"))

            val name =
                this.queue.name + " " + Conts.formatDateBig.format(this.queue.startDate) + " " + Calendar.getInstance()
                    .timeInMillis + ".csv"

            val exportDir = File(Conts.APP_DIRECTORY, "")
            if (!exportDir.exists())
                exportDir.mkdirs()

            val file = File(exportDir, name)

            try {
                file.createNewFile()
                val csvWriter = CSVWriter(FileWriter(file))
                csvWriter.writeNext(arrayOf("Orden", "Nombre", "CI", "Fecha", "Hora"))
                list.forEachIndexed { index, list ->
                    csvWriter.writeNext(
                        arrayOf(
                            "$index",
                            list.name,
                            list.ci,
                            dateFormat.format(list.lastRegistry),
                            timeFormat.format(list.lastRegistry)
                        )
                    )
                }
                csvWriter.close()
                // Common.shareQueue(requireContext(), file, "csv")
                Toast.makeText(
                    requireContext(),
                    "La cola ${queue.name} ha sido exportada al almacenamiento del dispositivo",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun refreshAdapter() {

        _recyclerViewExportQueue.adapter = adapterSave

        val listOpen = mutableListOf<Queue>()
        val listSave = mutableListOf<Queue>()
        val list = viewModel.allQueues.value ?: ArrayList()
        list.map { queue ->
            if (!queue.isSaved) {
                listSave.add(queue)
            } else {
                listOpen.add(queue)
            }

        }


        adapterSave.contentList = listOpen


        adapterSave.notifyDataSetChanged()

        if (listSave.isNotEmpty() || listOpen.isNotEmpty()) {
            if (listSave.isNotEmpty()) {
                val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

            }

        }

        if (listOpen.isEmpty()) {
            _recyclerViewExportQueue.visibility = View.GONE
            val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            _recyclerViewExportQueue.layoutParams = lp

        } else {
            val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            _recyclerViewExportQueue.layoutParams = lp

            _recyclerViewExportQueue.visibility = View.VISIBLE

        }
    }


    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            title = "Exportar Colas"
            setTitleTextColor(resources.getColor(R.color.blue_drawer))

            setNavigationOnClickListener {
                requireActivity().title = requireContext().getString(R.string.app_name)
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                pop()
            }
        }
    }

    override fun onClick(queue: Queue) {

    }

    override fun onClickExport(list: List<Queue>) {

    }

    override fun onLongClick(view: View, queue: Queue) {

    }

    override fun onDownloadClick(queue: Queue, openQueue: Boolean) {

    }

    override fun onSaveClick(queue: Queue, delete: Boolean) {

    }


}