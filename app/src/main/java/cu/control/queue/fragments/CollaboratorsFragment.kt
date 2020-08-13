package cu.control.queue.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.R
import cu.control.queue.adapters.AdapterPerson
import cu.control.queue.dialogs.DialogAddCollaborator
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.room_queues.*
import me.yokeyword.fragmentation.SupportFragment

class CollaboratorsFragment(private val queue: Queue) : SupportFragment(){

    private lateinit var dao: Dao
    private val adapter = AdapterPerson(queue)

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
            DialogAddCollaborator(requireContext(), queue).create().show()
        }

        initToolBar()

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
                .subscribe{ list, error ->

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

            setNavigationIcon(R.drawable.ic_arrow_back)

            title = "Colaboradores - ${queue.name}"

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