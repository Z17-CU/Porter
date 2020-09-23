package cu.control.queue.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.reflect.TypeToken
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.adapters.AdapterMyColaborators
import cu.control.queue.dialogs.DialogAddCollaborator
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.my_colaborators.*
import kotlinx.android.synthetic.main.room_queues.toolbar
import me.yokeyword.fragmentation.SupportFragment

class MyCollaboratorsFragment() : SupportFragment() {

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

//        add_colaborator.setOnClickListener {
//            DialogAddCollaborator(requireContext()).create().show()
//        }

        initToolBar()

        initObserver()
    }

    private fun initObserver() {


        Completable.create {

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
//                this["operator"] =PreferencesManager(requireContext()).getCi()+"."+PreferencesManager(requireContext()).getFv()
                this["operator"] = "93110904905.ACY493422"
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            val result = APIService.apiService.getAllColaborators(
                headers = headerMap
            ).execute()

            if (result.code() == 200) {
                val listPerson = result.body()
                listPerson?.let { listperson ->

                    Single.create<List<Person>> {


                        it.onSuccess(listperson)
                    }.observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe { list, error ->
                            adapter.contentList = list
                            adapter.notifyDataSetChanged()
                        }.addTo(compositeDisposable = CompositeDisposable())


                }


            } else {
                requireActivity().runOnUiThread {
                    val errorBody = result.errorBody()?.string()
                    if (errorBody != null && result.code() == 405) {

                        val type = object : TypeToken<Person>() {

                        }.type


                    } else if (errorBody != null) {
                        when (result.code()) {
                            401 -> {

                            }
                            403 -> {
                                val dialog = Common.showHiErrorMessage(requireContext(), errorBody)
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
            .observeOn(Schedulers.computation())
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

                requireActivity().runOnUiThread {

                }
            }.addTo(compositeDisposable)

    }


    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            title = "Mis Colaboradores"
            setTitleTextColor(resources.getColor(R.color.blue_drawer))

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