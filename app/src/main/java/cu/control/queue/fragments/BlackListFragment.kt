package cu.control.queue.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.R
import cu.control.queue.adapters.AdapterClient
import cu.control.queue.dialogs.DialogInsertClient
import cu.control.queue.interfaces.OnClientClickListener
import cu.control.queue.interfaces.onSave
import cu.control.queue.repository.AppDataBase
import cu.control.queue.repository.Dao
import cu.control.queue.repository.entitys.Client
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.room_queues.*
import kotlinx.android.synthetic.main.room_queues._imageViewEngranes
import me.yokeyword.fragmentation.SupportFragment

class BlackListFragment : SupportFragment(), onSave, OnClientClickListener {

    private lateinit var dao: Dao
    private val adapter = AdapterClient(this)

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

        _fabAdd.setOnClickListener {
            DialogInsertClient(requireContext(), CompositeDisposable(), this).create().show()
        }

        initToolBar()

        initObserver()
    }

    private fun initObserver() {
        dao.getClientInBlackList().observe(viewLifecycleOwner, Observer {
            adapter.contentList = it
            adapter.notifyDataSetChanged()
            _imageViewEngranes.visibility = if (it.isEmpty())
                View.VISIBLE
            else
                View.GONE
        })
    }

    override fun save(client: Client) {
        Completable.create {
            client.onBlackList = true
            dao.insertClient(client)
            it.onComplete()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe().addTo(CompositeDisposable())
    }

    override fun onClick(client: Client) {

    }

    @SuppressLint("RestrictedApi")
    override fun onLongClick(view: View, client: Client) {
        val popupMenu = PopupMenu(requireContext(), view)
        (context as Activity).menuInflater.inflate(R.menu.menu_black_list, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Eliminar")
                        .setMessage("¿Desea eliminar a " + client.name + " de la lista negra?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            Completable.create {
                                client.onBlackList = false
                                dao.insertClient(client)
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

    @SuppressLint("RestrictedApi")
    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            inflateMenu(R.menu.import_export_black_list)

            if (this.menu is MenuBuilder)
                (this.menu as MenuBuilder).setOptionalIconsVisible(true)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_export -> {

                        true
                    }
                    R.id.action_import -> {

                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            setNavigationIcon(R.drawable.ic_arrow_back)

            title = "Lista Negra"

            setNavigationOnClickListener {
                requireActivity().title = requireContext().getString(R.string.app_name)
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                pop()
            }
        }
    }
}