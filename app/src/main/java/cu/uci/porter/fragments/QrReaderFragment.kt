package cu.uci.porter.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.zxing.Result
import cu.uci.porter.R
import cu.uci.porter.adapters.AdapterClient
import cu.uci.porter.dialogs.DialogTypeClient
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.utils.Common.Companion.getAge
import cu.uci.porter.utils.Common.Companion.getSex
import cu.uci.porter.utils.Progress
import cu.uci.porter.viewModels.ClientViewModel
import cu.uci.porter.viewModels.ClientViewModelFactory
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.qr_reader.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import me.yokeyword.fragmentation.SupportFragment
import java.util.*


class QrReaderFragment :
    SupportFragment(),
    ZXingScannerView.ResultHandler {

    private lateinit var viewModel: ClientViewModel

    private lateinit var dao: Dao
    private lateinit var progress: Progress
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.qr_reader, null)

        setHasOptionsMenu(true)

        progress = Progress(view.context)

        dao = AppDataBase.getInstance(view.context).dao()

        val tempViewModel: ClientViewModel by viewModels(
            factoryProducer = { ClientViewModelFactory(view.context) }
        )
        viewModel = tempViewModel

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _flash.setOnClickListener {
            _zXingScannerView.flash = !_zXingScannerView.flash
            turnFlash()
        }

        val adapter = AdapterClient()
        _recyclerViewClients.layoutManager = LinearLayoutManager(view.context)
        _recyclerViewClients.adapter = adapter

        viewModel.allClient.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            adapter.contentList = it
            adapter.notifyDataSetChanged()
            if (it.isNotEmpty()) {
                goTo(it.size - 1)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        resumeReader()
    }

    override fun onPause() {
        super.onPause()
        _zXingScannerView.flash = false
        _zXingScannerView.stopCamera()
        progress.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_insert_client -> {
                DialogTypeClient(progress.context, compositeDisposable).create().show()
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

            if (client == null) {
                showError(_flash.context.getString(R.string.readError))
            } else {

                if (dao.clientExist(client.id) > 0) {
                    done = false
                    showError(_flash.context.getString(R.string.clientExist))
                } else {
                    done = true
                    dao.insertClient(client)
                }
            }

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

    private fun resumeReader() {
        _zXingScannerView.stopCamera()
        _zXingScannerView.setResultHandler(this)
        _zXingScannerView.startCamera()
        turnFlash()
        progress.dismiss()
    }

    private fun showDone(done: Boolean?) {

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
                        Calendar.getInstance().timeInMillis
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
}