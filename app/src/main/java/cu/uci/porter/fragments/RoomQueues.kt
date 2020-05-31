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
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import cu.control.queue.utils.MediaUtil
import cu.uci.porter.R
import cu.uci.porter.adapters.AdapterQueue
import cu.uci.porter.dialogs.DialogCreateQueue
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.utils.Common
import cu.uci.porter.utils.Conts
import cu.uci.porter.utils.Progress
import cu.uci.porter.viewModels.ClientViewModel
import cu.uci.porter.viewModels.ClientViewModelFactory
import io.reactivex.disposables.CompositeDisposable
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.room_queues, null)

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

        _fabAdd.setOnClickListener {
            DialogCreateQueue(it.context, compositeDisposable).create().show()
        }

        _recyclerViewQueues.layoutManager = LinearLayoutManager(view.context)
        val adapter = AdapterQueue(this, viewModel)
        _recyclerViewQueues.adapter = adapter

        viewModel.allQueues.observe(viewLifecycleOwner, Observer {
            adapter.contentList = it
            adapter.notifyDataSetChanged()
            if (it.isNotEmpty()) {
                goTo(it.size - 1)
                _imageViewEngranes.visibility = View.GONE
            } else {
                _imageViewEngranes.visibility = View.VISIBLE
            }
        })
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)
        inflater.inflate(R.menu.import_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.settingst_layout, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        view._cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        view.editTextRotationTime.setText(
            sharedPreferences.getInt(
                Conts.QUEUE_TIME,
                Conts.DEFAULT_QUEUE_TIME_HOURS
            ).toString()
        )

        view._okButton.setOnClickListener {
            if (view.editTextRotationTime.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Debe insertar un valor.", Toast.LENGTH_LONG)
                    .show()
            } else {
                sharedPreferences.edit()
                    .putInt(Conts.QUEUE_TIME, view.editTextRotationTime.text.toString().toInt())
                    .apply()
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
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
}