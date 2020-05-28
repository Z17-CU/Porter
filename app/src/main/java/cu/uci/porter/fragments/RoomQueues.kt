package cu.uci.porter.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import cu.control.queue.utils.MediaUtil
import cu.uci.porter.R
import cu.uci.porter.adapters.AdapterQueue
import cu.uci.porter.dialogs.DialogCreateQueue
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.utils.Common
import cu.uci.porter.utils.Progress
import cu.uci.porter.viewModels.ClientViewModel
import cu.uci.porter.viewModels.ClientViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.room_queues.*
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.import_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> {
                pickQueue()
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
}