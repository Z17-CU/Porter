package cu.control.queue.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import cu.control.queue.R
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.jsonStruc.jsonStrucItem
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamCreateQueue
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamUpdateQueue
import cu.control.queue.utils.JsonWrite
import cu.control.queue.utils.PreferencesManager
import cu.control.queue.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._cancelButton
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._okButton
import kotlinx.android.synthetic.main.layout_dialog_insert_store.view.*
import java.util.*

class DialogCreateProvince(
    private val context: Context,
    private val compositeDisposable: CompositeDisposable,
    private val id: Long = -1L,
    private val clientViewModel: ClientViewModel,
    private val nameQueue: String,
    private val nameDescription: String

) {

    private lateinit var storeId: String
    private lateinit var dao: Dao
    private lateinit var dialog: AlertDialog
    private var queue: Queue? = null
    private var idProvince = 0
    private var idMunicipie = 0
    private var idStore = 0
    fun create(): AlertDialog {
        dao = AppDataBase.getInstance(context).dao()
        dialog = AlertDialog.Builder(context)
            .setView(getView())
            .setCancelable(false)
            .create()

        return dialog
    }


    @SuppressLint("LogNotTimber")
    private fun getView(): View {

        val view = View.inflate(context, R.layout.layout_dialog_insert_store, null)

        view._cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        view._okButton.setOnClickListener {

            if(idProvince!=PreferencesManager(context).getLastInfoCreateQueue()!!.split(",")[0].toInt()){
                idMunicipie=0
                idStore=0
                PreferencesManager(context).setLastInfoCreateQueue(idProvince,idMunicipie,idStore)
            }
            if (idProvince != 0 && idMunicipie != 0 && idStore != 0) {

                val resultReadJson = getCharts()

                val genericList = resultReadJson.map { it }

                storeId = genericList[idProvince].municipality[idMunicipie].store[idStore].id


                compositeDisposable.add(Completable.create {

                    val time = Calendar.getInstance().timeInMillis

                    val thisqueue = if (queue == null) {

                        Queue(
                            time,
                            nameQueue.trim(),
                            Calendar.getInstance().timeInMillis,
                            description = nameDescription.trim(),
                            uuid = PreferencesManager(context).getCi() + "-" + PreferencesManager(
                                context
                            ).getFv() + "-" + time,
                            created_date = time,
                            updated_date = time,
                            //Todo update this
                            business = 1,
                            province = "",
                            municipality = "",
                            collaborators = arrayListOf(PreferencesManager(context).getCi()),
                            owner = PreferencesManager(context).getCi()
                        )
                    } else {
                        queue!!.name = nameQueue.trim()
                        queue!!.description = nameDescription.trim()
                        queue!!
                    }
                    dao.insertQueue(thisqueue)

                    var tag = ""
                    val map = mutableMapOf<String, String>()
                    map[Param.KEY_QUEUE_NAME] = thisqueue.name
                    map[Param.KEY_QUEUE_DESCRIPTION] = thisqueue.description
                    val param = if (queue == null) {
                        tag = Param.TAG_CREATE_QUEUE
                        ParamCreateQueue(storeId, map, thisqueue.created_date ?: time)
                    } else {
                        tag = Param.TAG_UPDATE_QUEUE
                        ParamUpdateQueue(map, thisqueue.updated_date ?: time)
                    }

                    clientViewModel.onRegistreAction(thisqueue.uuid ?: "", param, tag, context)
                    PreferencesManager(context).setLastInfoCreateQueue(idProvince, idMunicipie, idStore)
                    it.onComplete()
                }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        dialog.dismiss()
                    }, {
                        it.printStackTrace()
                        showError(context.getString(R.string.error))
                    }))


            }
            else{
                Toast.makeText(context,"Debe seleccionar una provincia,municipio y tienda",Toast.LENGTH_SHORT).show()
            }

        }

        searchSpinnerProvince(view)

//        view._okButton.isEnabled = view._editTextName.text.toString().trim().isNotEmpty()

        if (id != -1L) {
            Single.create<Queue> {
                queue = dao.getQueue(id)
                it.onSuccess(queue!!)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { queue ->
//                    view._editTextName.setText(queue.name)
//                    view._editTextDescription.setText(queue.description)
//                    view._okButton.setText(context.getString(R.string.editar))
                }.addTo(compositeDisposable)
        }

        return view
    }

    private fun searchSpinnerProvince(view: View) {

        val resultReadJson = getCharts()

        val genericList = resultReadJson.map { it }


        val lastInfoCreateQueue = PreferencesManager(context).getLastInfoCreateQueue()
        if(lastInfoCreateQueue!=""){
            val split = lastInfoCreateQueue!!.split(",")
            idProvince = split[0].toInt()
            idMunicipie = split[1].toInt()
            idStore = split[2].toInt()
        }

        val data = genericList.map { it.name }.toString()
        view.spn_my_spinner.setItems(genericList.map { it.name }.toTypedArray())
        view.spn_my_spinner.setTitle("Seleccione una provincia")
        view.spn_my_spinner.setExpandTint(R.color.colorAccent)
        if (idProvince != 0 && idMunicipie != 0 && idStore != 0) {
            view.spn_store.visibility = View.VISIBLE
            view.spn_municipie.visibility = View.VISIBLE

            view.spn_my_spinner.hint = genericList[idProvince].name
            view.spn_municipie.hint = genericList[idProvince].municipality[idMunicipie].name
            view.spn_store.hint =
                genericList[idProvince].municipality[idMunicipie].store[idStore].name

        }
        view.spn_my_spinner.setOnItemClickListener { idPprovince ->
            idProvince = idPprovince
            view.spn_municipie.visibility = View.VISIBLE

            view.spn_municipie.setItems(genericList[idProvince].municipality.map {
                it.name
            }.toTypedArray())
            view.spn_municipie.setExpandTint(R.color.colorAccent)

            view.spn_municipie.setOnItemClickListener { idMunicipe ->
                idMunicipie = idMunicipe
                view.spn_store.visibility = View.VISIBLE
                view.spn_store.setItems(genericList[idProvince].municipality[idMunicipe].store.map { store ->
                    store.name
                }.toTypedArray())
                view.spn_store.setExpandTint(R.color.colorAccent)
                view.spn_store.setOnItemClickListener {
                    idStore=it
                    storeId = genericList[idProvince].municipality[idMunicipe].store[it].id
                    PreferencesManager(context).setLastInfoCreateQueue(idProvince, idMunicipie, it)
                }


            }
        }


    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }


    private fun getCharts(): List<jsonStrucItem> {

        val readFromFile = JsonWrite(context).readFromFile()
        val jsonString = context.assets.open("stores.json").bufferedReader().use {
            it.readText()
        }
        return readFromFile!!
    }
}