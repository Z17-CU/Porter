package cu.control.queue.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import cu.control.queue.R
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.dataBase.entitys.payload.jsonStruc.jsonStrucItem
import cu.control.queue.utils.JsonWrite
import cu.control.queue.utils.PreferencesManager
import cu.control.queue.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_select_store.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import me.yokeyword.fragmentation.SupportFragment

class CreateQueueSelectStoreFragment(
    private val clientViewModel: ClientViewModel
) : SupportFragment() {

    private lateinit var resultReadJson: List<jsonStrucItem>
    private lateinit var storeId: String
    private var idProvince = -1
    private var idMunicipie = -1
    private var idStore = -1

    private var compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.fragment_select_store, null)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        resultReadJson = getCharts()

        view._okButton.visibility = View.GONE
        view._okButtonText.visibility = View.VISIBLE

        view._okButtonText.setOnClickListener {

            val savedInfo = PreferencesManager(requireContext()).getLastInfoCreateQueue()!!

            if (savedInfo.isNotEmpty() && idProvince != savedInfo.split(",")[0].toInt()
            ) {
                idMunicipie = -1
                idStore = -1
                PreferencesManager(requireContext()).setLastInfoCreateQueue(
                    idProvince,
                    idMunicipie,
                    idStore
                )
            }
            if (idProvince != -1 && idMunicipie != -1 && idStore != -1) {


                val genericList = resultReadJson.map { it }

                storeId = genericList[idProvince].municipality[idMunicipie].store[idStore].id


                compositeDisposable.add(Completable.create {

                    val queue = clientViewModel.creatingQueue.value

                    queue!!.store = storeId

                    queue.province = resultReadJson[idProvince].name

                    queue.municipality = resultReadJson[idProvince].municipality[idMunicipie].name

                    queue.storeName = resultReadJson[idProvince].municipality[idMunicipie].store[idStore].name

                    clientViewModel.creatingQueue.postValue(queue)

                    PreferencesManager(requireContext()).setLastInfoCreateQueue(
                        idProvince,
                        idMunicipie,
                        idStore
                    )
                    it.onComplete()
                }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        pop()
                    }, {
                        it.printStackTrace()
                        showError(requireContext().getString(R.string.error))
                    }))


            } else {
                Toast.makeText(
                    context,
                    "Debe seleccionar una provincia, municipio y tienda",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        searchSpinnerProvince(view, resultReadJson)
    }

    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

            title = "Establecimiento"

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

    private fun getCharts(): List<jsonStrucItem> {
        val jsonString: String

        val storeVersionInit = PreferencesManager(requireContext()).getStoreVersionInit()
        if (!storeVersionInit) {
            jsonString = requireContext().assets.open("stores.json").bufferedReader().use {
                it.readText()
            }

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<List<jsonStrucItem>>() {}.type)

        } else {
            jsonString = JsonWrite(requireContext()).readFromFile()!!
            val gson: Gson = GsonBuilder().create()

            val porterHistruct: PorterHistruct =
                gson.fromJson(jsonString, PorterHistruct::class.java)

            val response = porterHistruct.stores

            return response!!
        }
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun searchSpinnerProvince(
        view: View,
        resultReadJson: List<jsonStrucItem>
    ) {


        val lastInfoCreateQueue = PreferencesManager(requireContext()).getLastInfoCreateQueue()
        if (lastInfoCreateQueue != "") {
            val split = lastInfoCreateQueue!!.split(",")
            idProvince = split[0].toInt()
            idMunicipie = split[1].toInt()
            idStore = split[2].toInt()
        }

        resultReadJson.map { it.name }.toString()
        view.spn_my_spinner.setItems(resultReadJson.map { it.name }.toTypedArray())
        view.spn_my_spinner.setTitle("Seleccione una provincia")

        view.spn_my_spinner.setExpandTint(R.color.colorAccent)
        if (idProvince != -1 && idMunicipie != -1 && idStore != -1) {
            view.spn_store.visibility = View.VISIBLE
            view.spn_municipie.visibility = View.VISIBLE
            view.spn_my_spinner.hint = resultReadJson[idProvince].name
            view.spn_municipie.hint = resultReadJson[idProvince].municipality[idMunicipie].name
            view.spn_store.hint =
                resultReadJson[idProvince].municipality[idMunicipie].store[idStore].name

        }
        view.spn_my_spinner.setOnItemClickListener { idPprovince ->
            idProvince = idPprovince
            view.spn_municipie.visibility = View.VISIBLE
            view.spn_store.visibility = View.VISIBLE

            view.spn_municipie.setItems(resultReadJson[idProvince].municipality.map {
                it.name
            }.toTypedArray())

            view.spn_municipie.setExpandTint(R.color.colorAccent)
            view.spn_municipie.setText(requireContext().getString(R.string.select_municipe))
            view.spn_store.setText(requireContext().getString(R.string.select_store))
            view.spn_store.visibility = View.VISIBLE

            view.spn_municipie.setTitle("Seleccione un municipio")
            view.spn_municipie.setOnItemClickListener { idMunicipe ->
                idMunicipie = idMunicipe
                view.spn_store.setItems(resultReadJson[idProvince].municipality[idMunicipe].store.map { store ->
                    store.name
                }.toTypedArray())
                view.spn_store.setExpandTint(R.color.colorAccent)
                view.spn_store.setTitle("Seleccione una tienda")
                view.spn_store.setText(requireContext().getString(R.string.select_store))
                view.spn_store.setOnItemClickListener {
                    idStore = it
                    storeId = resultReadJson[idProvince].municipality[idMunicipe].store[it].id
                    PreferencesManager(requireContext()).setLastInfoCreateQueue(
                        idProvince,
                        idMunicipie,
                        idStore
                    )

                }
            }

            view.spn_municipie.hint = "Seleccione un municipio"
            view.spn_store.hint = "Seleccione una tienda"

        }
    }
}