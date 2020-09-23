package cu.control.queue.fragments

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.adapters.AdapterMyColaborators
import cu.control.queue.dialogs.DialogAddCollaborator
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import cu.control.queue.utils.PreferencesManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.my_colaborators.*
import kotlinx.android.synthetic.main.toolbar.*
import me.yokeyword.fragmentation.SupportFragment

class MyCollaboratorsFragment : SupportFragment() {

    private lateinit var dao: Dao
    private val adapter = AdapterMyColaborators()
    private val compositeDisposable = CompositeDisposable()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.my_colaborators, null)

        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        dao = AppDataBase.getInstance(view.context).dao()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _recyclerViewMyColaborators.layoutManager = LinearLayoutManager(view.context)
        _recyclerViewMyColaborators.adapter = adapter

        _okButton.setOnClickListener {
            DialogAddCollaborator(requireContext()).create().show()
        }

        initToolBar()

        initObserver()

        swipeContainer.setOnRefreshListener {
            updateColaborators()
        }
    }

    private fun initObserver() {

        dao.getAllCollaborators().observe(viewLifecycleOwner, Observer {
            adapter.contentList = it
            adapter.notifyDataSetChanged()
        })

        updateColaborators()
    }

    private fun updateColaborators() {
        Completable.create {

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
                this["operator"] =
                    PreferencesManager(requireContext()).getCi() + "." + PreferencesManager(
                        requireContext()
                    ).getFv()
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            val result = APIService.apiService.getAllColaborators(
                headers = headerMap
            ).execute()

            if (result.code() == 200) {
                val listPerson = result.body()
                listPerson?.let {
                    dao.insertCollaborator(listPerson as ArrayList<Person>)
                }

            } else {
                if (this@MyCollaboratorsFragment.isVisible)
                    requireActivity().runOnUiThread {
                        val errorBody = result.errorBody()?.string()
                        if (errorBody != null && result.code() == 405) {

                        } else if (errorBody != null) {
                            when (result.code()) {
                                401 -> {

                                }
                                403 -> {
                                    val dialog =
                                        Common.showHiErrorMessage(requireContext(), errorBody)
                                    dialog.show()
                                }
                                404 -> {

                                }

                            }
                        }
                    }
            }

            it.onComplete()
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorComplete {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        it.message ?: "Error de red",
                        Toast.LENGTH_LONG
                    ).show()
                }

                true
            }
            .subscribe {
                swipeContainer?.let {
                    swipeContainer.isRefreshing = false
                }
            }.addTo(compositeDisposable)
    }

    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            title = "Mis Colaboradores"
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
}