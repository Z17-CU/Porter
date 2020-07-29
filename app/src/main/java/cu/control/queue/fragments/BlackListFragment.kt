package cu.control.queue.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.utils.Common
import cu.control.queue.utils.Conts
import cu.control.queue.utils.MediaUtil
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.room_queues.*
import me.yokeyword.fragmentation.SupportFragment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
                when (requestCode) {
                    PICK_FILE_BLACK_LIST -> {
                        val file = File(MediaUtil.getPath(requireContext(), data.data!!) ?: "")
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
                                val list = Common.stringToListClient(text.toString())
                                list?.let {
                                    Completable.create {
                                        dao.insertClient(list)
                                        it.onComplete()
                                    }.subscribeOn(Schedulers.io())
                                        .observeOn(Schedulers.io())
                                        .subscribe()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    requireContext(),
                                    "Archivo incorrecto o lista corrupta.",
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
                        export()
                        true
                    }
                    R.id.action_import -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            this.type = "*/*"
                        }

                        startActivityForResult(intent, PICK_FILE_BLACK_LIST)
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

    private fun export() {
        val file = File(Conts.APP_DIRECTORY)
        if (!file.exists()) {
            file.mkdir()
        }

        try {
            val gpxfile = File(
                file,
                "Lista negra " + " " + Calendar.getInstance()
                    .timeInMillis + ".blackList"
            )
            val data = Common.clientListToString(adapter.contentList)
            val writer = FileWriter(gpxfile)
            writer.append(data)
            writer.flush()
            writer.close()
            (context as Activity).runOnUiThread {
                Toast.makeText(
                    context,
                    R.string.export_OK_black_list,
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val PICK_FILE_BLACK_LIST = 2
    }
}