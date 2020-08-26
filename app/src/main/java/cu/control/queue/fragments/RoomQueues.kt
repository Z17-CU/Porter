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
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.SettingsActivity
import cu.control.queue.adapters.AdapterQueue
import cu.control.queue.dialogs.DialogCreateQueue
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Hi403Message
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_ADD_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_CHECKED
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_MEMBER_UPDATED_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NUMBER
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_REINTENT_COUNT
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_UNCHECKED
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamDeleteQueue
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamUpdateQueue
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
import org.json.JSONObject
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

        //viewModel.observePayloads(viewLifecycleOwner)
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

    override fun onSaveClick(queue: Queue) {
        Completable.create {
            dao.getPayload(queue.uuid!!)?.let {
                viewModel.sendPayloads(listOf(it))
            }
            it.onComplete()
        }.observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe().addTo(compositeDisposable)
    }

    override fun onDownloadClick(queue: Queue, openQueue: Boolean) {
        progress.show()
        Completable.create {

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
                this["collaborator"] = PreferencesManager(requireContext()).getId()
                this["queue"] = queue.uuid!!
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            val result = APIService.apiService.getQueue(
                headers = headerMap
            ).execute()

            if (result.code() == 200) {
                val thisQueue = result.body()
                thisQueue?.let { it ->
                    val savedQueue = dao.getQueueByUUID(it.uuid)!!

                    var owner = ""

                    dao.deleteAllClientsFromQueue(savedQueue.id!!)

                    it.members?.map { person ->

                        val name = (person.info["name"]
                            ?: person.ci + " " + person.info["last_name"]) as String
                        val client = Client(
                            name,
                            person.ci.toLong(),
                            person.ci,
                            person.fv,
                            Common.getSex(person.ci),
                            Common.getAge(person.ci),
                            ((person.info[KEY_MEMBER_UPDATED_DATE] as Double?)
                                ?: person.info[KEY_ADD_DATE] as Double? ?: 0.toDouble()).toLong(),
                            ((person.info[KEY_REINTENT_COUNT] as Double?) ?: 0.toDouble()).toInt(),
                            ((person.info[KEY_NUMBER] as Double?) ?: 0.toDouble()).toInt(),
                            (person.info[KEY_CHECKED] as Double?) ?: 0.toDouble() > (person.info[KEY_UNCHECKED] as Double?) ?: 0.toDouble()
                        )

                        dao.insertClient(client)

                        val clientInQueue = ClientInQueue(
                            (person.info[KEY_ADD_DATE] as Double? ?: 0.toDouble()).toLong(),
                            savedQueue.id ?: it.created_date,
                            client.id,
                            (person.info[KEY_MEMBER_UPDATED_DATE] as Double?
                                ?: person.info[KEY_ADD_DATE] as Double? ?: 0.toDouble()).toLong(),
                            client.reIntent,
                            client.number,
                            client.isChecked
                        )

                        dao.insertClientInQueue(clientInQueue)
                    }

                    savedQueue.collaborators = ArrayList()

                    it.operators?.map { person ->
                        if (person.info[Person.KEY_AFFILIATION] == "owner")
                            owner = person.ci
                        savedQueue.collaborators.add(person.ci)
                    }

                    it.operators?.let {
                        dao.insertCollaborator(it)
                    }

                    savedQueue.business = it.store
                    savedQueue.clientsNumber = it.members?.size ?: 0
                    savedQueue.downloaded = true
                    savedQueue.owner = owner
                    savedQueue.isSaved = false
                    savedQueue.id = savedQueue.id ?: it.created_date

                    dao.insertQueue(savedQueue)

                    val map = mutableMapOf<String, String>()
                    map[Param.KEY_QUEUE_NAME] = savedQueue.name
                    map[Param.KEY_QUEUE_DESCRIPTION] = savedQueue.description

                    viewModel.onRegistreAction(
                        savedQueue.uuid!!,
                        ParamUpdateQueue(map, Calendar.getInstance().timeInMillis),
                        Param.TAG_UPDATE_QUEUE,
                        requireContext()
                    )

                    requireActivity().runOnUiThread {
                        showReaderOptions(queue)
                    }
                }
            } else {
                requireActivity().runOnUiThread {
                    val errorBody = result.errorBody()?.string()
                    if (errorBody != null && result.code() == 405) {

                        val type = object : TypeToken<Person>() {

                        }.type

                        val person: Person = Gson().fromJson(errorBody, type)

                        val message =
                            "La cola está siendo operada por ${person.info[Person.KEY_NAME]} ${person.info[Person.KEY_LAST_NAME]}."

                        AlertDialog.Builder(requireContext())
                            .setTitle("Cola en uso")
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, null)
                            .setNeutralButton("Abrir") { _, _ ->
                                showDialogWorkOffline(
                                    queue,
                                    openQueue,
                                    "Cola en uso",
                                    "La cola está siendo usada"
                                )
                            }
                            .create().show()
                    } else if (errorBody != null) {
                        showDialogWorkOffline(queue, openQueue)
                        Toast.makeText(requireContext(), errorBody, Toast.LENGTH_LONG).show()
                    } else {
                        showDialogWorkOffline(queue, openQueue)
                        Toast.makeText(requireContext(), result.message(), Toast.LENGTH_LONG).show()
                    }
                }
            }

            it.onComplete()
        }.subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .onErrorComplete {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        it.message ?: "Error de red",
                        Toast.LENGTH_LONG
                    ).show()
                }
                showDialogWorkOffline(queue, openQueue)
                true
            }
            .subscribe {
                requireActivity().runOnUiThread {
                    progress.dismiss()
                }
            }.addTo(compositeDisposable)
    }

    override fun onClick(queue: Queue) {

        if (!queue.downloaded && !queue.isOffline) {
            downloadQueueDialog(queue, true)
            return
        } else if (queue.isSaved && !queue.isOffline) {
            onDownloadClick(queue, true)
            return
        }

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

    private fun downloadQueueDialog(queue: Queue, openMode: Boolean = false) {
        AlertDialog.Builder(requireContext())
            .setTitle("Descargar cola")
            .setMessage("Debe descargar la cola antes de continuar.")
            .setPositiveButton("Descargar") { _, _ ->
                onDownloadClick(queue, openMode)
            }.setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }

    override fun onLongClick(view: View, queue: Queue) {
        if (!queue.downloaded && !queue.isOffline) {
            downloadQueueDialog(queue)
            return
        } else if (!queue.isSaved && !queue.isOffline) {
            saveDialog(queue)
            return
        }

        showPopup(view, queue)
    }

    private fun saveDialog(queue: Queue) {
        AlertDialog.Builder(requireContext())
            .setTitle("Guardar")
            .setMessage("Debe guardar la cola antes de continuar.")
            .setPositiveButton("Guardar") { _, _ ->
                onSaveClick(queue)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(view: View, queue: Queue) {

        val popupMenu = PopupMenu(requireContext(), view)
        (context as Activity).menuInflater.inflate(R.menu.menu_delete, popupMenu.menu)
        popupMenu.menu.findItem(R.id.action_collaborators).isVisible = !queue.isOffline
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
                R.id.action_collaborators -> start(CollaboratorsFragment(queue))
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
                municipality = "",
                owner = PreferencesManager(requireContext()).getCi()
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

        progress.show()

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

            if (result.code() == 200) {
                result.body()?.let { body ->
                    val type = object : TypeToken<Map<String, Map<String, Any>>>() {

                    }.type

                    Gson().fromJson<Map<String, Map<String, Any>>>(body, type).map { entry ->

                        if (dao.getQueueByUUID(entry.key) == null) {
                            val name = entry.value["name"] as String
                            val description = entry.value["description"] as String
                            val createdDate = (entry.value["created_date"] as Double).toLong()
                            val tags = entry.value["tags"] as String

                            dao.insertQueue(
                                Queue(
                                    createdDate,
                                    name,
                                    createdDate,
                                    0,
                                    description,
                                    entry.key,
                                    null,
                                    null,
                                    null,
                                    createdDate,
                                    createdDate,
                                    ArrayList(),
                                    false,
                                    isSaved = true,
                                    owner = ""
                                )
                            )
                        }
                    }
                }
            }

            it.onSuccess(
                Pair(result.code(), result.errorBody()?.string() ?: result.message())
            )
        }
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn {
                Pair(200, "Error")
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.first != 200) {
                    val message = it.second ?: "Error ${it.first}"
                    val dialog = Common.showHiErrorMessage(requireContext(), message)
                    dialog.show()
                }
                progress.dismiss()
            }, {
                it.printStackTrace()
            }).addTo(compositeDisposable)
    }

    private fun showDialogWorkOffline(
        queue: Queue,
        openQueue: Boolean,
        title: String = "Error de Red",
        messageInit: String = "No se ha podido conectar al servidor"
    ) {

        requireActivity().runOnUiThread {
            val message =
                "$messageInit. En caso de seleccionar la opción LOCAL la aplicación creará una copia local de la cola que no prodrá ser guardada en el servidor."
            AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Reintentar") { _, _ ->
                    onDownloadClick(queue, openQueue)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton("Local") { _, _ ->

                    Completable.create {

                        val oldQueueId = queue.id
                        var time = Calendar.getInstance().timeInMillis
                        queue.id = time
                        queue.uuid =
                            PreferencesManager(requireContext()).getCi() + "-" + PreferencesManager(
                                requireContext()
                            ).getFv() + "-" + time
                        queue.name = "Copia Local - ${queue.name}"
                        queue.description = "Copia Local de ${queue.description}"
                        queue.owner = PreferencesManager(requireContext()).getCi()
                        queue.collaborators = ArrayList()
                        queue.downloaded = true
                        queue.isSaved = true
                        queue.isOffline = true

                        val clientsInThisQueue = ArrayList<ClientInQueue>()

                        dao.getClientsInQueueList(oldQueueId!!).map {
                            it.id = time++
                            it.queueId = queue.id!!
                            clientsInThisQueue.add(it)
                        }

                        dao.insertQueue(queue)
                        dao.insertClientInQueue(clientsInThisQueue)

                    }.observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe()
                        .addTo(compositeDisposable)

                }.create().show()
        }
    }

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}