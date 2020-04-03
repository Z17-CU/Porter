package cu.uci.porter.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import cu.uci.porter.R
import cu.uci.porter.adapters.AdapterQueue
import cu.uci.porter.dialogs.DialogCreateQueue
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.utils.Progress
import cu.uci.porter.viewModels.ClientViewModel
import cu.uci.porter.viewModels.ClientViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.room_queues.*
import me.yokeyword.fragmentation.SupportFragment

class RoomQueues : SupportFragment() {

    private lateinit var viewModel: ClientViewModel

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

    private fun goTo(pos: Int) {

        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }
        smoothScroller.targetPosition = pos
        _recyclerViewQueues.layoutManager?.startSmoothScroll(smoothScroller)
    }
}