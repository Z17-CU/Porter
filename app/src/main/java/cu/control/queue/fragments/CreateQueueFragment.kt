package cu.control.queue.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import cu.control.queue.R
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamCreateQueue
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamUpdateQueue
import cu.control.queue.utils.PreferencesManager
import cu.control.queue.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_queue.view.*
import kotlinx.android.synthetic.main.room_queues.*
import kotlinx.android.synthetic.main.toolbar.view.*
import me.yokeyword.fragmentation.SupportFragment
import java.util.*
import kotlin.collections.ArrayList

class CreateQueueFragment(
    private val clientViewModel: ClientViewModel,
    private val id: Long = -1L
) : SupportFragment() {

    private lateinit var dao: Dao
    private var compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.fragment_create_queue, null)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        dao = AppDataBase.getInstance(view.context).dao()

        return view
    }

    override fun onBackPressedSupport(): Boolean {
        hideSoftInput()
        clientViewModel.creatingQueue.postValue(null)
        return super.onBackPressedSupport()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        if (id != -1L) {
            Single.create<Queue> {
                it.onSuccess(dao.getQueue(id))
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { queue ->
                    clientViewModel.creatingQueue.postValue(queue)
                }.addTo(compositeDisposable)
        } else {
            clientViewModel.creatingQueue.postValue(null)
        }

        clientViewModel.creatingQueue.observe(viewLifecycleOwner, Observer {
            it?.let { queue ->
                view._editTextName.setText(queue.name)
                view._editTextDescription.setText(queue.description)

                var textProducts = ""
                queue.info?.get(Person.KEY_PRODUCTS)?.let {
                    it as ArrayList<*>
                    var isFirst = true
                    it.map { product ->
                        if (isFirst) {
                            isFirst = false
                        } else {
                            textProducts += ", "
                        }
                        textProducts += product as String
                    }
                }
                view._editTextProducts.setText(textProducts)

                view.selectStore.text =
                    if (queue.store.isNullOrEmpty()) {
                        requireContext().getText(R.string.sin_definir)
                    } else {
                        "${queue.province}, ${queue.municipality}, ${queue.storeName}"
                    }

                view.alertSwitch.isChecked = queue.alert ?: true
            }
        })

        view._okButton.setOnClickListener {
            val thisqueue = createQueue(view)
            val time = Calendar.getInstance().timeInMillis

            when {
                thisqueue?.store == null -> {
                    Toast.makeText(
                        it.context,
                        "Debe seleccionar un establecimiento.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                view._editTextName.text?.trim()?.isEmpty() ?: false -> {
                    Toast.makeText(
                        it.context,
                        "Es necesario el nombre de la cola.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    Completable.create {

                        dao.insertQueue(thisqueue)

                        val map = mutableMapOf<String, Any>()
                        thisqueue.info?.let {
                            map[Param.KEY_QUEUE_PRODUCTS] =
                                it[Param.KEY_QUEUE_PRODUCTS] as ArrayList<*>? ?: ArrayList<String>()
                        }
                        val tag: String
                        map[Param.KEY_QUEUE_NAME] = thisqueue.name
                        map[Param.KEY_QUEUE_DESCRIPTION] = thisqueue.description
                        val param = if (id == -1L) {
                            tag = Param.TAG_CREATE_QUEUE
                            ParamCreateQueue(thisqueue.store!!, map, thisqueue.created_date ?: time)
                        } else {
                            tag = Param.TAG_UPDATE_QUEUE
                            ParamUpdateQueue(map, thisqueue.updated_date ?: time)
                        }

                        clientViewModel.onRegistreAction(
                            thisqueue.uuid ?: "",
                            param,
                            tag,
                            requireContext()
                        )
                        it.onComplete()
                    }.subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            clientViewModel.creatingQueue.postValue(null)
                            pop()
                        }.addTo(compositeDisposable)
                }
            }
        }

        view.selectStore.setOnClickListener {
            createQueue(view)
            start(CreateQueueSelectStoreFragment(clientViewModel))
        }
    }

    private fun createQueue(view: View): Queue {

        val time = Calendar.getInstance().timeInMillis
        val productsQueue = view._editTextProducts.text.trim().toString()
        val products: ArrayList<String>
        products = if (productsQueue.contains(",")) {
            productsQueue.split(',').map(String::trim).toList() as java.util.ArrayList<String>
        } else {
            arrayListOf(productsQueue.trim())
        }

        val map = mutableMapOf<String, Any>()
        map[Param.KEY_QUEUE_PRODUCTS] = products

        val queue = clientViewModel.creatingQueue.value

        val thisqueue = if (queue == null) {

            Queue(
                time,
                view._editTextName.text.toString().trim(),
                Calendar.getInstance().timeInMillis,
                description = view._editTextDescription.text.toString().trim(),
                uuid = PreferencesManager(requireContext()).getCi() + "-" + PreferencesManager(
                    requireContext()
                ).getFv() + "-" + time,
                created_date = time,
                updated_date = time,
                collaborators = arrayListOf(PreferencesManager(requireContext()).getCi()),
                owner = PreferencesManager(requireContext()).getCi(),
                info = map,
                alert = view.alertSwitch.isChecked
            )
        } else {
            queue.alert = view.alertSwitch.isChecked
            queue.name = view._editTextName.text.toString().trim()
            queue.description = view._editTextDescription.text.toString().trim()
            if (queue.info == null) {
                queue.info = map
            } else {
                (queue.info as MutableMap)[Param.KEY_QUEUE_PRODUCTS] = products
            }
            queue
        }

        clientViewModel.creatingQueue.postValue(thisqueue)

        return thisqueue
    }

    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

            title = "Nueva Cola"

            setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))

            setNavigationOnClickListener {
                hideSoftInput()
                requireActivity().title = requireContext().getString(R.string.app_name)
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                clientViewModel.creatingQueue.postValue(null)
                pop()
            }
        }
    }
}