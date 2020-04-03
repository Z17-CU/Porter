package cu.uci.porter.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.zxing.Result
import cu.uci.porter.MainActivity
import cu.uci.porter.R
import cu.uci.porter.adapters.AdapterClient
import cu.uci.porter.dialogs.DialogInsertClient
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.Queue
import cu.uci.porter.utils.Common.Companion.getAge
import cu.uci.porter.utils.Common.Companion.getSex
import cu.uci.porter.utils.PDF
import cu.uci.porter.utils.Progress
import cu.uci.porter.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.qr_reader.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import me.yokeyword.fragmentation.SupportFragment
import java.util.*


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
    private lateinit var observer: Observer<List<Client>>

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

        observer = androidx.lifecycle.Observer {
            adapter.contentList = it
            adapter.notifyDataSetChanged()
            if (it.isNotEmpty()) {
                goTo(it.size - 1)
                _imageViewEngranes.visibility = View.GONE
            } else {
                _imageViewEngranes.visibility = View.VISIBLE
            }
        }

        viewModel.allClients.removeObserver(observer)

        viewModel.setAllClient(queue.id!!)

        viewModel.allClients.observe(viewLifecycleOwner, observer)

        currentMode.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            try {
                menu.findItem(R.id.action_list).icon = ContextCompat.getDrawable(
                    _recyclerViewClients.context, if (it == MODE_LIST) {
                        pauseScanner()
                        _qrReader.visibility = View.GONE
                        menu.findItem(R.id.action_show_filter_menu).isVisible = true
                        menu.findItem(R.id.action_save).isVisible = true
                        menu.findItem(R.id.action_insert_client).isVisible = false
                        R.drawable.ic_qr_reader
                    } else {
                        _qrReader.visibility = View.VISIBLE
                        resumeReader()
                        menu.findItem(R.id.action_show_filter_menu).isVisible = false
                        menu.findItem(R.id.action_save).isVisible = false
                        menu.findItem(R.id.action_insert_client).isVisible = true

                        viewModel.allClients.removeObserver(observer)

                        viewModel.setAllClient(queue.id!!)

                        viewModel.allClients.observe(viewLifecycleOwner, observer)
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
                DialogInsertClient(progress.context, compositeDisposable, queue.id!!, this).create()
                    .show()
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
                PDF(requireContext()).write(queue, adapter.contentList)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun handleResult(rawResult: Result) {

        progress.show()

        //play sound
        val mp = MediaPlayer.create(context, R.raw.beep)
        mp.start()
        //vibrate
        val vibratorService = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(80)

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

    private fun setRangue(min: Int, max: Int) {
        Log.d("setRangue", "$min to $max")

        viewModel.allClients.removeObserver(observer)

        viewModel.setAllClientsInRangue(queue.id!!, min, max)

        viewModel.allClients.observe(viewLifecycleOwner, observer)
    }

    @SuppressLint("RestrictedApi")
    private fun showPopupMenu(menuItem: MenuItem) {
        val view: View = requireActivity().findViewById(menuItem.itemId)
        val popupMenu = PopupMenu(requireContext(), view)
        (context as MainActivity).menuInflater.inflate(R.menu.menu_filters, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter_todos -> {
                    viewModel.allClients.removeObserver(observer)

                    viewModel.setAllClient(queue.id!!)

                    viewModel.allClients.observe(viewLifecycleOwner, observer)
                }
                R.id.action_filter_niños -> {
                    setRangue(-1, 12)
                }
                R.id.action_filter_jovenes -> {
                    setRangue(13, 30)
                }
                R.id.action_filter_adultos -> {
                    setRangue(31, 55)
                }
                R.id.action_filter_3raEdad -> {
                    setRangue(56, 200)
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
            client.id += queue.id!!
            if (dao.clientExist(client.id, queue.id!!) > 0) {
                done = false
                showError(_flash.context.getString(R.string.clientExist))

                val mClient = dao.getClient(client.id)
                mClient.reIntent++
                mClient.lastRegistry = Calendar.getInstance().timeInMillis
                dao.insertClient(mClient)
            } else {
                done = true
                dao.insertClient(client)
            }

            val queue = dao.getQueue(queue.id!!)
            queue.clientsNumber = dao.clientsByQueue(queue.id!!)

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
                _imageViewCheck
            } else {
                _imageViewFail
            }
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .translationY(0f)
                .alpha(1.0f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        view.animate()
                            .translationY(0f)
                            .alpha(0.0f)
                            .setDuration(200)
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
                        name,
                        lastName,
                        id,
                        idString,
                        fv,
                        sex,
                        getAge(idString),
                        Calendar.getInstance().timeInMillis,
                        queue.id!!
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

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}