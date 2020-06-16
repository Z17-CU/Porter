package cu.uci.porter.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.miguelcatalan.materialsearchview.MaterialSearchView
import cu.control.queue.utils.MediaUtil
import cu.uci.porter.R
import cu.uci.porter.SettingsActivity
import cu.uci.porter.adapters.AdapterQueue
import cu.uci.porter.dialogs.DialogCreateQueue
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.repository.entitys.Queue
import cu.uci.porter.utils.Common
import cu.uci.porter.utils.Conts
import cu.uci.porter.utils.Progress
import cu.uci.porter.viewModels.ClientViewModel
import cu.uci.porter.viewModels.ClientViewModelFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.about_as.view.*
import kotlinx.android.synthetic.main.room_queues.*
import kotlinx.android.synthetic.main.settingst_layout.view.*
import me.yokeyword.fragmentation.SupportFragment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class RoomQueues : SupportFragment() {

    private lateinit var viewModel: ClientViewModel

    private val PICK_FILE_CODE = 1

    private lateinit var dao: Dao
    private lateinit var progress: Progress
    private val compositeDisposable = CompositeDisposable()

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        _fabAdd.setOnClickListener {
            DialogCreateQueue(it.context, compositeDisposable).create().show()
        }

        _recyclerViewQueues.layoutManager = LinearLayoutManager(view.context)
        adapter = AdapterQueue(this, viewModel)
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
    }

    override fun onBackPressedSupport(): Boolean {
        return if (searchView.isSearchOpen) {
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

    @SuppressLint("RestrictedApi")
    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            inflateMenu(R.menu.import_menu)

            val item = this.menu.findItem(R.id.action_search)
            searchView.setMenuItem(item)

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
                    R.id.action_abaut -> {
                        showAboutAs()
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

            searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
                override fun onSearchViewShown() {
                    searchView.findViewById<EditText>(R.id.searchTextView).inputType =
                        InputType.TYPE_CLASS_NUMBER
                    searchView.findViewById<EditText>(R.id.searchTextView).filters = arrayOf(
                        InputFilter.LengthFilter(11)
                    )
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
//        val inflater =
//            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val view = inflater.inflate(R.layout.settingst_layout, null)
//        val alertDialog = AlertDialog.Builder(requireContext())
//            .setView(view)
//            .create()
//        view._cancelButton.setOnClickListener {
//            alertDialog.dismiss()
//        }
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
//        view.editTextRotationTime.setText(
//            sharedPreferences.getInt(
//                Conts.QUEUE_TIME,
//                Conts.DEFAULT_QUEUE_TIME_HOURS
//            ).toString()
//        )
//
//        view._okButton.setOnClickListener {
//            if (view.editTextRotationTime.text.toString().isEmpty()) {
//                Toast.makeText(requireContext(), "Debe insertar un valor.", Toast.LENGTH_LONG)
//                    .show()
//            } else {
//                sharedPreferences.edit()
//                    .putInt(Conts.QUEUE_TIME, view.editTextRotationTime.text.toString().toInt())
//                    .apply()
//                alertDialog.dismiss()
//            }
//        }
//        alertDialog.show()
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

    private fun <T : Any?> MutableLiveData<T>.default(initialValue: T?) =
        apply { setValue(initialValue) }
}