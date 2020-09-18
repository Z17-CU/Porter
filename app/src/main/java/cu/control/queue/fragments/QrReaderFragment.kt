package cu.control.queue.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.reflect.TypeToken
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.turingtechnologies.materialscrollbar.CustomIndicator
import cu.control.queue.MainActivity
import cu.control.queue.R
import cu.control.queue.SettingsActivity
import cu.control.queue.adapters.AdapterClient
import cu.control.queue.adapters.SwipeToDeleteCallback
import cu.control.queue.dialogs.DialogInsertClient
import cu.control.queue.interfaces.OnClientClickListener
import cu.control.queue.interfaces.onSave
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_ADD_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_CHECKED
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_DELETE_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_LAST_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_MEMBER_UPDATED_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NUMBER
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_PRODUCTS
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_REINTENT_COUNT
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_UNCHECKED
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.MODE_ADD_OWNER
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.MODE_CHECK
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.MODE_INCREMENT_REINTENT
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.MODE_UNCHECK
import cu.control.queue.repository.dataBase.entitys.payload.params.Param.Companion.TAG_ADD_MEMBER
import cu.control.queue.repository.dataBase.entitys.payload.params.Param.Companion.TAG_DELETE_MEMBER
import cu.control.queue.repository.dataBase.entitys.payload.params.Param.Companion.TAG_UPDATE_MEMBER
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamAddMember
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamDeleteMember
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamGeneral
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamUpdateMember
import cu.control.queue.utils.*
import cu.control.queue.utils.Conts.Companion.ALERTS
import cu.control.queue.utils.Conts.Companion.APP_DIRECTORY
import cu.control.queue.utils.Conts.Companion.DEFAULT_QUEUE_TIME_HOURS
import cu.control.queue.utils.Conts.Companion.QUEUE_CANT
import cu.control.queue.utils.Conts.Companion.QUEUE_TIME
import cu.control.queue.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_rangue_select.view.*
import kotlinx.android.synthetic.main.qr_reader.*
import me.bendik.simplerangeview.SimpleRangeView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import me.yokeyword.fragmentation.SupportFragment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class QrReaderFragment(
    thisQueue: Queue,
    private val viewModel: ClientViewModel,
    private val checkList: Boolean
) :
    SupportFragment(),
    ZXingScannerView.ResultHandler,
    OnClientClickListener,
    onSave {

    companion object {
        const val MODE_READ = 1
        const val MODE_LIST = 2
    }

    private var queue = thisQueue

    private var currentMode = MutableLiveData<Int>().default(MODE_READ)
    private lateinit var menu: Menu
    private lateinit var dao: Dao
    private lateinit var progress: Progress
    private val compositeDisposable = CompositeDisposable()
    private val adapter = AdapterClient(this)

    private var searchQuery = MutableLiveData<String>().default("")

    private var client: Client? = null
    private var done: Boolean? = null

    private var exportFrom = 0
    private var exportTo = 0
    private var exportInRangue = false

    private var isAddClient = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.qr_reader, null)

        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        progress = Progress(view.context)

        dao = AppDataBase.getInstance(view.context).dao()

        adapter.checkMode = checkList

        return view
    }

    @SuppressLint("LogNotTimber")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        _showAddClient.setOnClickListener {
            showDialogInsertClient()
        }

        _recyclerViewClients.layoutManager = LinearLayoutManager(view.context)
        _recyclerViewClients.adapter = adapter
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter, requireContext()))
        itemTouchHelper.attachToRecyclerView(_recyclerViewClients)
        dragScrollBar.setIndicator(CustomIndicator(requireContext()), true)

        updateObserver(queue.id!!)

        currentMode.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            try {
                menu.findItem(R.id.action_list).icon = ContextCompat.getDrawable(
                    _recyclerViewClients.context, if (it == MODE_LIST) {
                        pauseScanner()
                        _qrReader.visibility = View.GONE
                        menu.findItem(R.id.action_show_filter_menu).isVisible = true
                        menu.findItem(R.id.action_list).title =
                            requireContext().getString(R.string.readQR)
                        R.drawable.ic_qr_reader
                    } else {
                        _qrReader.visibility = View.VISIBLE
                        resumeReader()
                        menu.findItem(R.id.action_show_filter_menu).isVisible = false
                        menu.findItem(R.id.action_list).title =
                            requireContext().getString(R.string.lista)
                        updateObserver(queue.id!!)

                        R.drawable.ic_filter_list
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        searchQuery.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                val id = it.toLong()
                val index = adapter.contentList.indexOfLast { client -> client.id == id }
                adapter.contentList.map { client ->
                    client.searched = false
                }
                if (index != -1) {
                    adapter.contentList[index].searched = true
                    goTo(index)
                } else {
                    showError("El cliente no está en la cola.")
                }
                adapter.notifyDataSetChanged()
            }
        })

        dao.getQueueLive(queue.id!!).observe(viewLifecycleOwner, Observer {
            queue = it
            menu.findItem(R.id.action_save_online).isVisible = !it.isSaved
        })

        resumeReader()

    }

    override fun onResume() {
        super.onResume()
        progress.dismiss()
    }

    override fun onPause() {
        super.onPause()
        pauseScanner()
    }

    override fun onBackPressedSupport(): Boolean {
        return if (searchView.isOpen) {
            searchView.closeSearch()
            true
        } else if (!queue.isSaved) {
            AlertDialog.Builder(requireContext())
                .setTitle("Cambios sin guardar")
                .setMessage("Hay cambios sin guardar en la cola actual. ¿Desea guardarlos?")
                .setPositiveButton("Guardar") { _, _ ->
                    sendPayloads()
                }
                .setNegativeButton("Salir") { _, _ ->
                    requireActivity().title = requireContext().getString(R.string.app_name)
                    (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                        false
                    )
                    pop()
                }.create().show()
            true
        } else {
            requireActivity().title = requireContext().getString(R.string.app_name)
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                false
            )
            pop()
            true
        }
    }

    override fun handleResult(rawResult: Result) {

        //play sound
        val mp = MediaPlayer.create(context, R.raw.beep)
        mp.start()
        //vibrate
        val vibratorService = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(120)

        compositeDisposable.add(Completable.create {

            val client = Common.stringToClient(rawResult)

            saveClient(client)

            it.onComplete()
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                resumeReader()
            }, {
                it.printStackTrace()
                resumeReader()
            }))
    }

    @SuppressLint("RestrictedApi")
    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            inflateMenu(R.menu.main)

            val item = this.menu.findItem(R.id.action_search)

            if (this.menu is MenuBuilder)
                (this.menu as MenuBuilder).setOptionalIconsVisible(true)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_list -> {
                        changeMode()
                        true
                    }
                    R.id.action_show_filter_menu -> {
                        showPopupMenu(item)
                        true
                    }
                    R.id.action_save -> {
                        selectRangeToExport()
                        true
                    }
                    R.id.action_save_online -> {
                        sendPayloads()
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

            setNavigationIcon(R.drawable.ic_arrow_back)

            title = queue.name

            setNavigationOnClickListener {
                onBackPressedSupport()
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

            this@QrReaderFragment.menu = this.menu
        }
    }

    private fun sendPayloads() {
        progress.show()
        Completable.create {
            dao.getPayload(queue.uuid!!)?.let {
                viewModel.sendPayloads(listOf(it))
            }
            it.onComplete()
        }.observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe {
                progress.dismiss()
                pop()
            }.addTo(compositeDisposable)
    }

    @SuppressLint("LogNotTimber")
    private fun updateObserver(
        queueId: Long,
        isInRange: Boolean = false,
        min: Int = 0,
        max: Int = 0
    ) {

        viewModel.allClientsInQueue.removeObservers(viewLifecycleOwner)
        viewModel.setAllClientsIDQueue(queueId)
        viewModel.allClientsInQueue.observe(
            viewLifecycleOwner,
            Observer { clientsInQueue ->

                Single.create<List<Client>> { singleEmitter ->

                    var idList: List<Long> = ArrayList()
                    clientsInQueue.map {
                        idList = idList + it.clientId
                        Log.d("ClientId", it.clientId.toString())
                    }
                    var clientList: List<Client> = ArrayList()
                    if (isInRange) {
                        if (idList.size < 1000) {
                            clientList = dao.getClientsInRange(idList, min, max)
                        } else {
                            var a = 0
                            var b = 999

                            while (a < idList.size) {
                                if (b > idList.size) {
                                    b = idList.size
                                }
                                clientList = clientList + dao.getClientsInRange(
                                    idList.subList(a, b),
                                    min,
                                    max
                                )
                                a = b
                                b += 999
                            }
                        }
                    } else {
                        clientList = dao.getClients(idList)
                    }

                    clientList.map { client ->
                        val thisClientInQueue = clientsInQueue.find { it.clientId == client.id }!!
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

                        adapter.contentList = clients
                        adapter.queueId = queueId
                        adapter.notifyDataSetChanged()
                        if (clients.isNotEmpty()) {
                            goTo()
                            _imageViewEngranes.visibility = View.GONE
                        } else {
                            _imageViewEngranes.visibility = View.VISIBLE
                        }
                        toolbar.title = queue.name + " - " + adapter.contentList.size

                    }.addTo(compositeDisposable)
            })
    }

    @SuppressLint("RestrictedApi")
    private fun showPopupMenu(menuItem: MenuItem) {
        val view: View = requireActivity().findViewById(menuItem.itemId)
        val popupMenu = PopupMenu(requireContext(), view)
        (context as MainActivity).menuInflater.inflate(R.menu.menu_filters, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter_todos -> {
                    updateObserver(queueId = queue.id!!)
                }
                R.id.action_filter_niños -> {
                    updateObserver(queue.id!!, true, -1, 12)
                }
                R.id.action_filter_jovenes -> {
                    updateObserver(queue.id!!, true, 13, 30)
                }
                R.id.action_filter_adultos -> {
                    updateObserver(queue.id!!, true, 31, 55)
                }
                R.id.action_filter_3raEdad -> {
                    updateObserver(queue.id!!, true, 56, 200)
                }
            }
            false
        }
        val wrapper = ContextThemeWrapper(context, R.style.PopupWhite)
        val menuPopupHelper = MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, view)
        menuPopupHelper.setForceShowIcon(true)
        menuPopupHelper.show()
    }

    private fun changeMode() {
        if (currentMode.value == MODE_READ) {
            currentMode.postValue(MODE_LIST)
        } else {
            currentMode.postValue(MODE_READ)
        }
    }

    private fun pauseScanner() {
        _zXingScannerView.flash = false
        _zXingScannerView.stopCamera()
        progress.dismiss()
    }

    private fun saveClient(client: Client?) {
        this.client = client
        isAddClient = true
        if (client == null) {
            showError(_showAddClient.context.getString(R.string.readError))
            return
        }

        if (dao.getClient(client.id)?.onBlackList == true) {

            MediaPlayer.create(context, R.raw.error_buzz).start()

            requireActivity().runOnUiThread {
                AlertDialog.Builder(requireContext())
                    .setTitle("Lista negra")
                    .setMessage("${client.name} está en lista negra.")
                    .setPositiveButton(requireContext().getString(android.R.string.ok), null)
                    .create().show()
            }
            return
        }

        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ALERTS, false) && !checkList
        ) {

            val queueCant = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(QUEUE_CANT, DEFAULT_QUEUE_TIME_HOURS)

            val currentTime = Calendar.getInstance().timeInMillis
            val startDate = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(SettingsActivity.QUERY_START_DATE, currentTime)
            val endDate = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(SettingsActivity.QUERY_END_DATE, currentTime)

            val count = dao.countToAlert(
                client.id,
                startDate,
                endDate
            )
            if (count >= queueCant) {

                MediaPlayer.create(context, R.raw.error_buzz).start()

                requireActivity().runOnUiThread {

                    AlertDialog.Builder(requireContext())
                        .setTitle("Alerta")
                        .setMessage(
                            client.name + " ha lanzado una alerta porque del día ${Conts.formatDateMid.format(
                                startDate
                            )} al ${Conts.formatDateMid.format(endDate)} ha estado en $count colas."
                        )
                        .setPositiveButton("Continuar") { _, _ ->
                            Completable.create {
                                proccesClient(client)
                                it.onComplete()
                            }.subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribe()
                                .addTo(compositeDisposable)
                        }
                        .setNegativeButton("Cancelar", null)
                        .create().show()
                }
            } else {
                proccesClient(client)
            }

        } else {
            proccesClient(client)
        }
    }

    private fun proccesClient(client: Client): Boolean {
        if (checkList) {
            val tempClient = dao.getClientFromQueue(client.id, queue.id!!)
            if (tempClient == null) {
                showError("El cliente no está en la cola.")
                showDone(false)
                return false
            }

            var clientInQueu: ClientInQueue? = null
            done = if (tempClient.isChecked) {
                dao.getClientFromQueue(client.id, queue.id!!)?.let {
                    it.reIntent++
                    it.lastRegistry = Calendar.getInstance().timeInMillis
                    clientInQueu = it
                    dao.insertClientInQueue(it)
                    payloadUpdateMember(clientInQueu!!, client, MODE_INCREMENT_REINTENT)
                }
                false
            } else {
                tempClient.isChecked = true
                clientInQueu = tempClient
                dao.insertClientInQueue(tempClient)
                payloadUpdateMember(clientInQueu!!, client, MODE_CHECK)
                true
            }
        } else {
            dao.insertClient(client)

            done = true

            var clientInQueue = dao.getClientFromQueue(client.id, queue.id!!)

            queue.clientsNumber = dao.clientsByQueue(queue.id!!)
            queue.clientsNumber++

            if (clientInQueue == null) {
                clientInQueue =
                    ClientInQueue(
                        Calendar.getInstance().timeInMillis,
                        queue.id!!,
                        client.id,
                        Calendar.getInstance().timeInMillis,
                        number = dao.getLastNumberInQueue(queue.id!!) + 1
                    )
                payloadAddMember(clientInQueue, client)
            } else {
                if ((Calendar.getInstance().timeInMillis - clientInQueue.lastRegistry) < (PreferenceManager.getDefaultSharedPreferences(
                        context
                    ).getInt(QUEUE_TIME, DEFAULT_QUEUE_TIME_HOURS)) * 60 * 60 * 1000
                ) {
                    clientInQueue.reIntent++
                    queue.clientsNumber--
                    done = false
                    payloadUpdateMember(clientInQueue, client, MODE_INCREMENT_REINTENT)
                }
                clientInQueue.lastRegistry = Calendar.getInstance().timeInMillis
            }
            dao.insertClientInQueue(clientInQueue)

            dao.insertQueue(queue)
        }
        showDone(done)
        return done!!
    }

    private fun payloadAddMember(clientInQueue: ClientInQueue, client: Client) {

        Completable.create {

            val payload = dao.getPayload(queue.uuid!!)
            var param: ParamAddMember? = payload?.methods?.get(TAG_ADD_MEMBER) as ParamAddMember?

            val nameArray = client.name!!.split(' ')
            val name: String
            var lastName = ""
            val products = ""
            if (nameArray.size > 1) {
                name = nameArray[0]
                var pos = 0
                nameArray.map {
                    if (pos != 0) {
                        lastName += "$it "
                    }
                    pos++
                }
            } else {
                name = client.name!!
                lastName = ""
            }

            val map = mutableMapOf<String, Any>()
            map[KEY_ADD_DATE] = clientInQueue.lastRegistry
            map[KEY_REINTENT_COUNT] = clientInQueue.reIntent.toLong()
            map[KEY_NUMBER] = clientInQueue.number.toLong()
            map[KEY_NAME] = name.trim()
            map[KEY_LAST_NAME] = lastName.trim()
            map[KEY_PRODUCTS] = if (queue.info != null) {
                if ((queue.info!![KEY_PRODUCTS] as ArrayList<*>?).isNullOrEmpty()) {
                    ArrayList<String>()
                } else {
                    queue.info!![KEY_PRODUCTS] as ArrayList<*>
                }
            } else {
                ArrayList<String>()
            }

            val type = object : TypeToken<Map<String, ParamGeneral>>() {

            }.type

            val person = Person(client.ci, client.fv ?: "", map)

            if (param != null) {
                param.person.add(person)
            } else {
                param = ParamAddMember(arrayListOf(person))
            }


            viewModel.onRegistreAction(queue.uuid!!, param, TAG_ADD_MEMBER, requireContext())

            it.onComplete()
        }.observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe().addTo(compositeDisposable)
    }

    private fun payloadUpdateMember(clientInQueue: ClientInQueue, client: Client, mode: String) {

        Completable.create {

            val payload = dao.getPayload(queue.uuid!!)
            var param: ParamUpdateMember? =
                payload?.methods?.get(TAG_UPDATE_MEMBER) as ParamUpdateMember?

            val map = mutableMapOf<String, Long>()

            val time = Calendar.getInstance().timeInMillis
            map[KEY_MEMBER_UPDATED_DATE] = time
            when (mode) {
                MODE_CHECK -> map[KEY_CHECKED] = time
                MODE_UNCHECK -> map[KEY_UNCHECKED] = time
                MODE_INCREMENT_REINTENT -> map[KEY_REINTENT_COUNT] =
                    clientInQueue.reIntent.toLong()
                MODE_ADD_OWNER -> map[KEY_NUMBER] = clientInQueue.number.toLong()
            }

            val person = Person(client.ci, client.fv ?: "", map)

            if (param != null) {
                param.person.add(person)
            } else {
                param = ParamUpdateMember(arrayListOf(person))
            }

            viewModel.onRegistreAction(queue.uuid!!, param, TAG_UPDATE_MEMBER, requireContext())

            it.onComplete()
        }.observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe().addTo(compositeDisposable)
    }

    private fun payloadDeleteMember(client: Client) {

        Completable.create {

            val payload = dao.getPayload(queue.uuid!!)
            var param: ParamDeleteMember? =
                payload?.methods?.get(TAG_DELETE_MEMBER) as ParamDeleteMember?

            val map = mutableMapOf<String, Long>()
            map[KEY_DELETE_DATE] = Calendar.getInstance().timeInMillis

            val person = Person(client.ci, client.fv ?: "", map)

            if (param != null) {
                param.person.add(person)
            } else {
                param = ParamDeleteMember(arrayListOf(person))
            }

            viewModel.onRegistreAction(queue.uuid!!, param, TAG_DELETE_MEMBER, requireContext())

            it.onComplete()
        }.observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe().addTo(compositeDisposable)
    }

    private fun resumeReader() {
        if (currentMode.value == MODE_READ) {
            _zXingScannerView.stopCamera()
            _zXingScannerView.setResultHandler(this)
            _zXingScannerView.startCamera()
        }
        progress.dismiss()
    }

    private fun showDone(done: Boolean?) {

        Completable.create {
            done?.let {
                if (done) {
                    MediaPlayer.create(context, R.raw.access_granted).start()
                } else {
                    MediaPlayer.create(context, R.raw.access_denied).start()
                }
            }
            it.onComplete()
        }.observeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .subscribe().addTo(compositeDisposable)
    }

    private fun goTo(goTo: Int = -1) {
        var pos = -1

        if (goTo == -1 && !isAddClient)
            return

        isAddClient = false

        Handler().postDelayed({
            val smoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_END
                }
            }
            pos = if (goTo != -1) {
                goTo
            } else if (client != null && done != null) {
                adapter.contentList.indexOfFirst { it.id == client!!.id }
            } else {
                adapter.contentList.size - 1
            }
            if (pos == -1) {
                pos = adapter.contentList.size - 1
            }
            smoothScroller.targetPosition = pos
            _recyclerViewClients.layoutManager?.startSmoothScroll(smoothScroller)
        }, 100)

        Handler().postDelayed({
            _recyclerViewClients.layoutManager?.scrollToPosition(pos)
            if (done != null) {
                adapter.done = done!!
                adapter.contentList[pos].selected = true
                adapter.notifyDataSetChanged()
            }
        }, 1000)
    }

    private fun showError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(_zXingScannerView.context, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun showSaveOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.layout_buttom_sheet_dialog)
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<TextView>(R.id._optionSavePDF)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            try {
                val list = adapter.contentList.subList(exportFrom, exportTo)
                PDF(requireContext()).write(queue, list)
            } catch (e: NoSuchFileException) {
                e.printStackTrace()
                showError("La función de exportar no es compatible con su dispositivo.")
            }
        }

        bottomSheetDialog.findViewById<TextView>(R.id._optionSaveCSV)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            try {
                exportQueueCSV()
            } catch (e: NoSuchFileException) {
                e.printStackTrace()
                showError("La función de exportar no es compatible con su dispositivo.")
            }
        }

        bottomSheetDialog.findViewById<TextView>(R.id._optionSaveJSON)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            try {
                exportQueueJson()
            } catch (e: NoSuchFileException) {
                e.printStackTrace()
                showError("La función de exportar no es compatible con su dispositivo.")
            }
        }
    }

    private fun showDialogInsertClient() {
        pauseScanner()
        val dialog = DialogInsertClient(
            progress.context,
            compositeDisposable,
            this@QrReaderFragment
        ).create()
        dialog.setOnDismissListener {
            resumeReader()
        }
        dialog.show()
    }

    private fun exportQueueCSV() {
        if (adapter.contentList.isNotEmpty()) {
            val list = adapter.contentList.subList(exportFrom, exportTo)
            val timeFormat = SimpleDateFormat("h:mm a", Locale("es", "CU"))
            val dateFormat = SimpleDateFormat("d 'de' MMM 'del' yyyy", Locale("es", "CU"))

            val name =
                queue.name + " " + Conts.formatDateBig.format(queue.startDate) + " " + Calendar.getInstance()
                    .timeInMillis + ".csv"

            val exportDir = File(APP_DIRECTORY, "")
            if (!exportDir.exists())
                exportDir.mkdirs()

            val file = File(exportDir, name)

            try {
                file.createNewFile()
                val csvWriter = CSVWriter(FileWriter(file))
                csvWriter.writeNext(arrayOf("Orden", "Nombre", "CI", "Fecha", "Hora"))
                list.forEachIndexed { index, client ->
                    csvWriter.writeNext(
                        arrayOf(
                            "$index",
                            client.name,
                            client.ci,
                            dateFormat.format(client.lastRegistry),
                            timeFormat.format(client.lastRegistry)
                        )
                    )
                }
                csvWriter.close()
                Common.shareQueue(requireContext(), file, "csv")
                Toast.makeText(
                    requireContext(),
                    R.string.export_OK,
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun exportQueueJson() {
        Dexter.withActivity(requireActivity())
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        queue.let {
                            Single.create<Queue> {

                                queue.clientInQueueList =
                                    dao.getClientsInQueueList(queueId = queue.id!!)
                                var clientsIds: List<Long> = ArrayList()
                                queue.clientInQueueList!!.map { clientInQueue ->
                                    clientsIds = clientsIds + clientInQueue.clientId
                                }

                                queue.clientList = dao.getClients(clientsIds)

                                it.onSuccess(queue)
                            }.subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribe { queue, _ ->
                                    viewModel.exportQueue(queue, requireContext())
                                }
                                .addTo(compositeDisposable)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }

    override fun save(client: Client) {
        Completable.create {
            saveClient(client)
            it.onComplete()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe().addTo(compositeDisposable)
    }

    override fun onClick(client: Client) {

    }

    @SuppressLint("RestrictedApi")
    override fun onLongClick(view: View, client: Client) {
        val context = view.context
        val dao = AppDataBase.getInstance(context).dao()
        val popupMenu = PopupMenu(context, view)
        (context as Activity).menuInflater.inflate(R.menu.menu_client, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Eliminar")
                        .setMessage("¿Desea eliminar a " + client.name + " de la lista?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            Completable.create {
                                payloadDeleteMember(client)
                                val size = adapter.contentList.size - 1
                                dao.deleteClientFromQueue(client.id, adapter.queueId)
                                dao.updateQueueSize(adapter.queueId, size)
                                it.onComplete()
                            }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe().addTo(CompositeDisposable())
                        }
                        .create()
                        .show()
                }
                R.id.action_black_list -> {
                    Completable.create {
                        client.isChecked = false
                        client.selected = false
                        client.searched = false
                        client.repeatedClient = false
                        client.onBlackList = true
                        dao.insertClient(client)
                        it.onComplete()
                    }.observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .subscribe {
                            showError("${client.name} añadido a lista negra.")
                        }.addTo(compositeDisposable)
                }
                R.id.action_check -> {
                    val message =
                        "Desea actualizar a ${client.name} como ${if (client.isChecked) "no chequeado" else "chequeado"}?"
                    android.app.AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton("Actualizar") { _, _ ->
                            Completable.create { emitter ->
                                val clientInQueue =
                                    dao.getClientFromQueue(client.id, adapter.queueId)
                                clientInQueue?.isChecked = !client.isChecked
                                if (clientInQueue?.isChecked!!) {
                                    payloadUpdateMember(clientInQueue, client, MODE_CHECK)
                                } else {
                                    payloadUpdateMember(clientInQueue, client, MODE_UNCHECK)
                                }
                                dao.insertClientInQueue(clientInQueue)
                                emitter.onComplete()
                            }.observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe()
                        }
                        .create().show()
                }
            }
            false
        }
        val wrapper = androidx.appcompat.view.ContextThemeWrapper(context, R.style.PopupWhite)
        val menuPopupHelper =
            MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, view)
        menuPopupHelper.setForceShowIcon(true)
        menuPopupHelper.show()
    }

    override fun onSwipe(direction: Int, client: Client) {

        //vibrate
        val vibratorService =
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(120)

        Completable.create { emitter ->
            val clientInQueue =
                dao.getClientFromQueue(client.id, adapter.queueId)

            if (direction == ItemTouchHelper.RIGHT) {
                if (!clientInQueue!!.isChecked) {
                    clientInQueue.isChecked = true
                    payloadUpdateMember(clientInQueue, client, MODE_CHECK)
                }
            } else if (direction == ItemTouchHelper.LEFT) {
                if (clientInQueue!!.isChecked) {
                    clientInQueue.isChecked = false
                    payloadUpdateMember(clientInQueue, client, MODE_UNCHECK)
                }
            }
            dao.insertClientInQueue(clientInQueue!!)
            emitter.onComplete()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe().addTo(compositeDisposable)
    }

    private fun selectRangeToExport() {

        if (adapter.contentList.isEmpty())
            return

        exportInRangue = false

        val view = View.inflate(context, R.layout.layout_rangue_select, null)

        view.fixed_rangeview.showLabels = true

        view.fixed_rangeview.count = adapter.contentList[adapter.contentList.size - 1].number
        view.fixed_rangeview.start = adapter.contentList[0].number
        view.fixed_rangeview.end = adapter.contentList[adapter.contentList.size - 1].number

        exportTo = adapter.contentList.size
        exportFrom = 0

        view.startValue.text = adapter.contentList[0].number.toString()
        view.endValue.text = adapter.contentList[adapter.contentList.size - 1].number.toString()

        view.fixed_rangeview.onTrackRangeListener = object : SimpleRangeView.OnTrackRangeListener {
            @SuppressLint("SetTextI18n")
            override fun onEndRangeChanged(rangeView: SimpleRangeView, end: Int) {
                val pos = findPositionOfNumber(end + 1)
                exportTo = if (pos == -1) exportTo else pos + 1
                view.endValue.text = (end + 1).toString()

                view.endValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (pos == -1) {
                            view.endValue.text = "${end + 1} No existe"
                            R.color.google_red
                        } else R.color.colorAccent
                    )
                )

                exportInRangue = true
            }

            @SuppressLint("SetTextI18n")
            override fun onStartRangeChanged(rangeView: SimpleRangeView, start: Int) {
                val pos = findPositionOfNumber(start + 1)
                exportFrom = if (pos == -1) exportFrom else pos
                view.startValue.text = (start + 1).toString()

                view.startValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (pos == -1) {
                            view.startValue.text = "${start + 1} No existe"
                            R.color.google_red
                        } else R.color.colorAccent
                    )
                )

                exportInRangue = true
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->

                showSaveOptions()

            }
            .create().show()
    }

    private fun findPositionOfNumber(number: Int): Int {
        return adapter.contentList.indexOf(adapter.contentList.find { it.number == number })
    }
}