package cu.control.queue.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.adapters.AdapterPerson
import cu.control.queue.dialogs.DialogAddCollaborator
import cu.control.queue.interfaces.OnColaboratorClickListener
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.PreferencesManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.room_queues.*
import kotlinx.android.synthetic.main.toolbar.*
import me.yokeyword.fragmentation.SupportFragment

class CollaboratorsFragment(private val queue: Queue) : SupportFragment(), OnColaboratorClickListener {

    private lateinit var dao: Dao
    private val adapter = AdapterPerson(queue, this@CollaboratorsFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.room_queues, null)

        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        dao = AppDataBase.getInstance(view.context).dao()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _recyclerViewQueues.layoutManager = LinearLayoutManager(view.context)
        _recyclerViewQueues.adapter = adapter

        swipeContainer.isEnabled = false

        _fabAdd.setOnClickListener {
            DialogAddCollaborator(requireContext(), queue).create().show()
        }

        initToolBar()

        _okButton.visibility = View.GONE

        initObserver()
    }

    private fun initObserver() {
        dao.getAllQueues().observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            Single.create<List<Person>> {

                val idList = dao.getQueue(queue.id!!).collaborators
                val list = dao.getCollaborators(idList)

                it.onSuccess(list)
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { list, error ->

                    adapter.contentList = list
                    adapter.notifyDataSetChanged()
                    _imageViewEngranes.visibility = if (list.isEmpty())
                        View.VISIBLE
                    else
                        View.GONE

                }.addTo(compositeDisposable = CompositeDisposable())
        })
    }

    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            title = "Colaboradores - ${queue.name}"

            setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))

            setNavigationOnClickListener {
                requireActivity().title = requireContext().getString(R.string.app_name)
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                pop()
            }
        }
    }

    override fun onClick(colaborator: Person) {

    }

    override fun onLongClick(view: View, colaborator: Person) {
        showPopup(view, colaborator)
    }

    override fun onSwipe(direction: Int, colaborator: Person) {

    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(view: View, person: Person) {
        val context = view.context
        val dao = AppDataBase.getInstance(context).dao()
        val popupMenu = PopupMenu(context, view)
        (context as Activity).menuInflater.inflate(R.menu.menu_only_delete, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Eliminar")
                        .setMessage("¿Desea eliminar a " + person.info[Person.KEY_NAME] + " de la lista?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            Completable.create {

                                val headerMap = mutableMapOf<String, String>().apply {
                                    this["Content-Type"] = "application/json"
                                    this["operator"] = PreferencesManager(context).getId()
                                    this["queue"] = queue.uuid!!
                                    this["Authorization"] = Base64.encodeToString(
                                        BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                                    ) ?: ""
                                }

                                val result = APIService.apiService.deleteCollaborator(
                                    headers = headerMap,
                                    data = Gson().toJson(person)
                                ).execute()

                                if (result.code() == 200) {
                                    queue.collaborators.removeAll { ci -> ci == person.ci }
                                    dao.insertQueue(queue)
                                } else {
                                    context.runOnUiThread {
                                        Toast.makeText(context, result.message(), Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }

                                it.onComplete()
                            }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe().addTo(CompositeDisposable())
                        }
                        .create()
                        .show()
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
}