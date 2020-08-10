package cu.control.queue.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetDialog
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.SettingsActivity
import cu.control.queue.adapters.AdapterQueue
import cu.control.queue.dialogs.DialogCreateQueue
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamDeleteQueue
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.*
import cu.control.queue.viewModels.ClientViewModel
import cu.control.queue.viewModels.ClientViewModelFactory
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.about_as.view.*
import kotlinx.android.synthetic.main.room_queues.*
import me.yokeyword.fragmentation.SupportFragment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.ArrayList

class RoomQueues : SupportFragment(), onClickListener {

    private lateinit var viewModel: ClientViewModel

    private val PICK_FILE_CODE = 1

    private lateinit var dao: Dao
    private lateinit var progress: Progress
    private val compositeDisposable = CompositeDisposable()

    private var toMerge = false
    private var queueToMerge: Queue? = null

    private lateinit var adapter: AdapterQueue

    private var searchQuery = MutableLiveData<String>().default("")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.room_queues, null)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        setHasOptionsMenu(true)

        progress = Progress(view.context)

        dao = AppDataBase.getInstance(view.context).dao()

        val tempViewModel: ClientViewModel by viewModels(
            factoryProducer = { ClientViewModelFactory(view.context) }
        )
        viewModel = tempViewModel

        sendHi()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        _fabAdd.setOnClickListener {
            DialogCreateQueue(it.context, compositeDisposable, clientViewModel = viewModel).create()
                .show()
        }

        _recyclerViewQueues.layoutManager = LinearLayoutManager(view.context)
        adapter = AdapterQueue(this)
        _recyclerViewQueues.adapter = adapter

        viewModel.allQueues.observe(viewLifecycleOwner, Observer {
            searchView.closeSearch()
            refreshAdapter(it)
        })

        searchQuery.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                refreshAdapter(viewModel.allQueues.value ?: ArrayList())
            } else {
                Single.create<List<Queue>> { emitter ->

                    val list: List<Queue> =
                        dao.getQueuesByIds(dao.getQueuesIdsByClient(it.toLong()) ?: ArrayList())
                            ?: ArrayList()

                    emitter.onSuccess(list)
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { list ->
                        refreshAdapter(list)
                    }.addTo(compositeDisposable)
            }
        })

        viewModel.observePayloads(viewLifecycleOwner)
    }

    override fun onBackPressedSupport(): Boolean {
        return if (searchView.isOpen) {
            searchView.closeSearch()
            true
        } else {
            super.onBackPressedSupport()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
                when (requestCode) {
                    PICK_FILE_CODE -> {
                        val file = File(MediaUtil.getPath(requireContext(), data.data!!))
                        if (file.exists()) {
                            val text = StringBuilder()

                            val br = BufferedReader(FileReader(file))
                            var line: String?
                            while (br.readLine().also { line = it } != null) {
                                text.append(line)
                                text.append('\n')
                            }
                            br.close()

                            try {
                                val queue = Common.stringToQueue(text.toString())
                                queue?.let {
                                    viewModel.saveImportQueue(it)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    requireContext(),
                                    "Archivo incorrecto o cola corrupta.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No se encontró el archivo.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(queue: Queue) {
        if (toMerge && queueToMerge != null) {
            if (queue.id == queueToMerge!!.id) {
                Toast.makeText(
                    requireContext(),
                    "No puede mezclar una cola con ella misma.",
                    Toast.LENGTH_LONG
                ).show()
                queueToMerge = null
                toMerge = false
            } else {
                val startDate = if (queueToMerge!!.startDate < queue.startDate) {
                    queueToMerge!!.startDate
                } else {
                    queue.startDate
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(requireContext().getString(R.string.merge))
                    .setMessage(
                        "¿Está segur@ que desea mezclar la cola ${queueToMerge!!.name} con ${queue.name}? Se establecerá como fecha de la cola ${Conts.formatDateBig.format(
                            startDate
                        )} y los usuarios comunes solo aparecerán una vez. \nÉsta acción no se puede deshacer."
                    )
                    .setPositiveButton(requireContext().getText(R.string.merge)) { _, _ ->
                        mergeQueues(queueToMerge!!, queue, startDate)
                        queueToMerge = null
                        toMerge = false
                    }
                    .setNegativeButton(requireContext().getString(android.R.string.cancel)) { _, _ ->
                        queueToMerge = null
                        toMerge = false
                    }
                    .setCancelable(false)
                    .create().show()
            }
        } else {
            showReaderOptions(queue)
        }
    }

    override fun onLongClick(view: View, queue: Queue) {
        showPopup(view, queue)
    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(view: View, queue: Queue) {

        val popupMenu = PopupMenu(requireContext(), view)
        (context as Activity).menuInflater.inflate(R.menu.menu_delete, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialog.Builder(context)
                        .setTitle("Eliminar")
                        .setMessage("¿Desea eliminar " + queue.name + " de la lista?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            Completable.create {
                                dao.deleteQueue(queue)
                                dao.deleteAllClientsFromQueue(queue.id!!)

                                viewModel.onRegistreAction(
                                    queue.uuid ?: "",
                                    ParamDeleteQueue(Calendar.getInstance().timeInMillis),
                                    Param.TAG_DELETE_QUEUE,
                                    requireContext()
                                )

                                it.onComplete()
                            }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe()

                        }
                        .create()
                        .show()
                }
                R.id.action_edit -> {
                    DialogCreateQueue(
                        requireContext(),
                        CompositeDisposable(),
                        queue.id!!,
                        viewModel
                    ).create().show()
                }
                R.id.action_merge -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(requireContext().getString(R.string.merge))
                        .setMessage("Seleccione otra cola para mezclar con ${queue.name}.")
                        .setPositiveButton("Seleccionar") { _, _ ->
                            queueToMerge = queue
                            toMerge = true
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            queueToMerge = null
                        }.create().show()
                }
            }
            false
        }
        val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupWhite)
        val menuPopupHelper =
            MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, view)
        menuPopupHelper.setForceShowIcon(true)
        menuPopupHelper.show()
    }

    @SuppressLint("RestrictedApi")
    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            inflateMenu(R.menu.import_menu)

            if (this.menu is MenuBuilder)
                (this.menu as MenuBuilder).setOptionalIconsVisible(true)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_import -> {
                        pickQueue()
                        true
                    }
                    R.id.action_settings -> {
                        openSettings()
                        true
                    }
                    R.id.action_black_list -> {
                        start(BlackListFragment())
                        true
                    }
                    R.id.action_abaut -> {
                        showAboutAs()
                        true
                    }
                    R.id.action_search -> {
                        searchView.openSearch()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            //setNavigationIcon(R.drawable.ic_arrow_back)

            title = requireContext().getString(R.string.app_name)

            setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    //Do some magic
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    when (newText?.length) {
                        11 -> searchQuery.postValue(newText)
                        else -> searchQuery.postValue("")
                    }
                    return false
                }
            })

            searchView.setSearchViewListener(object : MaterialSearchView.SearchViewListener {
                override fun onSearchViewOpened() {

                }

                override fun onSearchViewClosed() {
                    searchQuery.postValue("")
                }
            })
        }
    }

    private fun refreshAdapter(list: List<Queue>) {
        adapter.contentList = list
        adapter.notifyDataSetChanged()
        if (list.isNotEmpty()) {
            goTo(list.size - 1)
            _imageViewEngranes.visibility = View.GONE
        } else {
            _imageViewEngranes.visibility = View.VISIBLE
        }
    }

    private fun goTo(pos: Int) {

        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }
        smoothScroller.targetPosition = pos
        _recyclerViewQueues.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun pickQueue() {
        viewModel.importQueue(this@RoomQueues, PICK_FILE_CODE)
    }

    private fun openSettings() {
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    private fun showReaderOptions(queue: Queue) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.layout_buttom_sheet_dialog_open_reader)
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<TextView>(R.id._optionAsChecker)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            start(QrReaderFragment(queue, viewModel, true))
        }

        bottomSheetDialog.findViewById<TextView>(R.id._optionAsReader)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            start(QrReaderFragment(queue, viewModel, false))
        }
    }

    private fun showAboutAs() {

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.about_as, null)
        view.versionName.text = getVersion()
        view._buttonSite.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(requireContext().resources.getString(R.string.https_github_com_richardanca_porter_by_richard))
                )
            )
        }

        AlertDialog.Builder(requireContext())
            .setView(view)
            .create().show()

    }

    @SuppressLint("SetTextI18n")
    private fun getVersion(): String {
        var pInfo: PackageInfo? = null
        var version: String? = ""
        var revision = 0
        try {
            pInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (pInfo != null) {
            version = pInfo.versionName
            revision = pInfo.versionCode
        }

        return "Versión: $version.$revision"
    }

    private fun mergeQueues(queue1: Queue, queue2: Queue, startDate: Long) {
        progress.show()
        Completable.create {

            val time = Calendar.getInstance().timeInMillis

            val newQueue = Queue(
                Calendar.getInstance().timeInMillis,
                "${queue1.name} y ${queue2.name}",
                startDate,
                description = when {
                    queue1.description!!.isEmpty() && queue2.description!!.isEmpty() -> ""
                    queue1.description!!.isNotEmpty() && queue2.description!!.isEmpty() -> queue1.description
                    queue1.description!!.isEmpty() && queue2.description!!.isNotEmpty() -> queue2.description
                    else -> "${queue1.description} y ${queue2.description}"
                },
                uuid = PreferencesManager(requireContext()).getCi() + "-" + PreferencesManager(
                    requireContext()
                ).getFv() + "-" + time,
                created_date = time,
                updated_date = time,
                //Todo update this
                business = 1,
                province = "",
                municipality = ""
            )

            val allClientsInQueue = dao.getClientInQueueBy2Queues(queue1.id!!, queue2.id!!)

            var number = 1
            allClientsInQueue.map { clientInQueue ->
                clientInQueue.number = number
                clientInQueue.queueId = newQueue.id!!
                number++
            }

            newQueue.clientsNumber = allClientsInQueue.size

            dao.getClientRepeatedClients(queue1.id!!, queue2.id!!).map { pair ->
                if (pair.second > 1) {
                    allClientsInQueue.find { client -> client.clientId == pair.first }?.repeatedClient =
                        true
                }
            }

            dao.deleteQueue(queue1)
            dao.deleteAllClientsFromQueue(queue1.id!!)
            dao.deleteQueue(queue2)
            dao.deleteAllClientsFromQueue(queue2.id!!)
            dao.insertQueue(newQueue)
            dao.insertClientInQueue(allClientsInQueue)

            it.onComplete()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                progress.dismiss()
            }
            .addTo(compositeDisposable)
    }

    private fun sendHi() {
        Single.create<Pair<Int, String?>> {

            val preferences = PreferencesManager(requireContext())

            val struct = PorterHistruct(
                preferences.getName(),
                preferences.getLastName(),
                preferences.getCi(),
                preferences.getFv()
            )

            val data = Common.porterHiToString(struct)

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            val result = APIService.apiService.hiPorter(
                data = data, headers = headerMap
            ).execute()
            it.onSuccess(
                Pair(result.code(), result.errorBody()?.string() ?: result.message())
            )
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.first != 200) {
                    val message = it.second ?: "Error ${it.first}"
                    val dialog = Common.showHiErrorMessage(requireContext(), message)
                    dialog.show()
                }
            }, {
                it.printStackTrace()
            }).addTo(compositeDisposable)
    }

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}