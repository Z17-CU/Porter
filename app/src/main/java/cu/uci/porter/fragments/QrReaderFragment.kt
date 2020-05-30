package cu.uci.porter.fragments

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import cu.uci.porter.MainActivity
import cu.uci.porter.R
import cu.uci.porter.adapters.AdapterClient
import cu.uci.porter.dialogs.DialogInsertClient
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.ClientInQueue
import cu.uci.porter.repository.entitys.Queue
import cu.uci.porter.utils.*
import cu.uci.porter.utils.Common.Companion.getAge
import cu.uci.porter.utils.Common.Companion.getSex
import cu.uci.porter.utils.Conts.Companion.APP_DIRECTORY
import cu.uci.porter.utils.Conts.Companion.DEFAULT_QUEUE_TIME_HOURS
import cu.uci.porter.utils.Conts.Companion.QUEUE_TIME
import cu.uci.porter.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.qr_reader.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import me.yokeyword.fragmentation.SupportFragment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class QrReaderFragment(private val queue: Queue, private val viewModel: ClientViewModel) :
    SupportFragment(),
    ZXingScannerView.ResultHandler {

    companion object {
        const val MODE_READ = 1
        const val MODE_LIST = 2
    }

    private var currentMode = MutableLiveData<Int>().default(MODE_READ)
    private lateinit var menu: Menu
    private lateinit var dao: Dao
    private lateinit var progress: Progress
    private val compositeDisposable = CompositeDisposable()
    private val adapter = AdapterClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.qr_reader, null)

        setHasOptionsMenu(true)

        requireActivity().title = queue.name

        progress = Progress(view.context)

        dao = AppDataBase.getInstance(view.context).dao()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _flash.setOnClickListener {
            _zXingScannerView.flash = !_zXingScannerView.flash
            turnFlash()
        }

        _recyclerViewClients.layoutManager = LinearLayoutManager(view.context)
        _recyclerViewClients.adapter = adapter

        updateObserver(queue.id!!)

        currentMode.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            try {
                menu.findItem(R.id.action_list).icon = ContextCompat.getDrawable(
                    _recyclerViewClients.context, if (it == MODE_LIST) {
                        pauseScanner()
                        _qrReader.visibility = View.GONE
                        menu.findItem(R.id.action_show_filter_menu).isVisible = true
                        menu.findItem(R.id.action_insert_client).isVisible = false
                        R.drawable.ic_qr_reader
                    } else {
                        _qrReader.visibility = View.VISIBLE
                        resumeReader()
                        menu.findItem(R.id.action_show_filter_menu).isVisible = false
                        menu.findItem(R.id.action_insert_client).isVisible = true

                        updateObserver(queue.id!!)

                        R.drawable.ic_filter_list
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        resumeReader()
        progress.dismiss()
    }

    override fun onPause() {
        super.onPause()
        pauseScanner()
    }

    override fun onBackPressedSupport(): Boolean {
        requireActivity().title = requireContext().getString(R.string.app_name)
        return super.onBackPressedSupport()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_insert_client -> {
                pauseScanner()
                val dialog = DialogInsertClient(
                    progress.context,
                    compositeDisposable,
                    queue.id!!,
                    this
                ).create()
                dialog.setOnDismissListener {
                    resumeReader()
                }
                dialog.show()
                true
            }
            R.id.action_list -> {
                changeMode()
                true
            }
            R.id.action_show_filter_menu -> {
                showPopupMenu(item)
                true
            }
            R.id.action_save -> {
                showSaveOptions()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun handleResult(rawResult: Result) {

        //play sound
        val mp = MediaPlayer.create(context, R.raw.beep)
        mp.start()
        //vibrate
        val vibratorService = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(120)

        var done: Boolean? = null

        compositeDisposable.add(Completable.create {

            val client = stringToClient(rawResult)

            done = saveClient(client)

            it.onComplete()
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                resumeReader()
                showDone(done)
            }, {
                it.printStackTrace()
                resumeReader()
            }))
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
                    var clientList = if (isInRange)
                        dao.getClientsInRange(idList, min, max)
                    else
                        dao.getClients(idList)

                    clientList.map { client ->
                        val thisClientInQueue = clientsInQueue.find { it.clientId == client.id }!!
                        client.lastRegistry = thisClientInQueue.lastRegistry
                        client.reIntent = thisClientInQueue.reIntent
                        client.number = thisClientInQueue.number
                    }

                    clientList = clientList.sortedBy { it.number }

                    singleEmitter.onSuccess(clientList)
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { clients, _ ->

                        adapter.contentList = clients
                        adapter.notifyDataSetChanged()
                        if (clients.isNotEmpty()) {
                            goTo(clients.size - 1)
                            _imageViewEngranes.visibility = View.GONE
                        } else {
                            _imageViewEngranes.visibility = View.VISIBLE
                        }

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

    fun saveClient(client: Client?): Boolean? {

        var done: Boolean? = null
        if (client == null) {
            showError(_flash.context.getString(R.string.readError))
        } else {
            dao.insertClient(client)
            done = true

            var clientInQueue = dao.getClientFromQueue(client.id, queue.id!!)

            queue.clientsNumber = dao.clientsByQueue(queue.id!!)
            queue.clientsNumber++

            if (clientInQueue == null) {
                clientInQueue =
                    ClientInQueue(
                        null,
                        queue.id!!,
                        client.id,
                        Calendar.getInstance().timeInMillis,
                        number = queue.clientsNumber
                    )
            } else {
                if ((Calendar.getInstance().timeInMillis - clientInQueue.lastRegistry) < (PreferenceManager.getDefaultSharedPreferences(
                        context
                    ).getInt(QUEUE_TIME, DEFAULT_QUEUE_TIME_HOURS)) * 60 * 60 * 1000
                ) {
                    clientInQueue.reIntent++
                    queue.clientsNumber--
                    done = false
                }
                clientInQueue.lastRegistry = Calendar.getInstance().timeInMillis
            }
            dao.insertClientInQueue(clientInQueue)

            dao.insertQueue(queue)
        }
        return done
    }

    private fun resumeReader() {
        if (currentMode.value == MODE_READ) {
            _zXingScannerView.stopCamera()
            _zXingScannerView.setResultHandler(this)
            _zXingScannerView.startCamera()
            turnFlash()
        }
        progress.dismiss()
    }

    fun showDone(done: Boolean?) {

        done?.let {
            _relativeDone.visibility = View.VISIBLE
            val view = if (done) {
                MediaPlayer.create(context, R.raw.access_granted).start()
                _imageViewCheck
            } else {
                MediaPlayer.create(context, R.raw.access_denied).start()
                _imageViewFail
            }
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .translationY(0f)
                .alpha(1.0f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        view.animate()
                            .translationY(0f)
                            .alpha(0.0f)
                            .setDuration(500)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    view.visibility = View.GONE
                                    _relativeDone.visibility = View.GONE
                                }
                            })
                    }
                })
        }
    }

    private fun goTo(pos: Int) {

        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }
        smoothScroller.targetPosition = pos
        _recyclerViewClients.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun showError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(_zXingScannerView.context, error, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("LogNotTimber")
    private fun stringToClient(rawResult: Result): Client? {

        var client: Client? = null

        rawResult.text?.let {

            Log.d("stringToClient", it)

            val name = Regex("N:(.+?)*").find(it)?.value?.split(':')?.get(1)
            val lastName = Regex("A:(.+?)*").find(it)?.value?.split(':')?.get(1)
            val idString = Regex("CI:(.+?)*").find(it)?.value?.split(':')?.get(1)
            val id = idString?.toLong()
            val sex = getSex(idString)
            val fv = Regex("FV:(.+?)*").find(it)?.value?.split(':')?.get(1)

            Log.d("Regex result", " \n$name\n$lastName\n$id\n$fv ")

            if (name != null && lastName != null && id != null && fv != null && sex != null) {

                client =
                    Client(
                        "$name $lastName",
                        id,
                        idString,
                        fv,
                        sex,
                        getAge(idString)
                    )
            }
        }

        return client
    }

    private fun turnFlash() {
        _flash.setImageDrawable(
            ContextCompat.getDrawable(
                _zXingScannerView.context,
                if (_zXingScannerView.flash) R.drawable.ic_flash_on else R.drawable.ic_flash_off
            )
        )
    }

    private fun showSaveOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.layout_buttom_sheet_dialog)
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<TextView>(R.id._optionSavePDF)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            PDF(requireContext()).write(queue, adapter.contentList)
        }

        bottomSheetDialog.findViewById<TextView>(R.id._optionSaveCSV)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            exportQueueCSV()
        }

        bottomSheetDialog.findViewById<TextView>(R.id._optionSaveJSON)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            exportQueueJson()
        }
    }

    private fun exportQueueCSV() {
        if (adapter.contentList.isNotEmpty()) {
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
                adapter.contentList.forEachIndexed { index, client ->
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
}