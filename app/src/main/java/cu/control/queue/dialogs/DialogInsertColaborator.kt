package cu.control.queue.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import cu.control.queue.utils.PreferencesManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view.*
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._cancelButton
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._editTextCI
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._editTextFV
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._okButton
import kotlinx.android.synthetic.main.layout_dialog_insert_manual_colaborator.view.*
import java.util.*
import kotlin.collections.HashMap

class DialogInsertColaborator(
    private val context: Context,
    private val queue: cu.control.queue.repository.dataBase.entitys.Queue,
    private val compositeDisposable: CompositeDisposable
) {

    private lateinit var dao: Dao
    private lateinit var dialog: AlertDialog

    fun create(): AlertDialog {
        dao = AppDataBase.getInstance(context).dao()
        dialog = AlertDialog.Builder(context,R.style.RationaleDialog)
            .setView(getView())
            .setCancelable(false)
            .create()

        return dialog
    }

    private fun getView(): View {

        val view = View.inflate(context, R.layout.layout_dialog_insert_client, null)

        view._cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        view._okButton.setOnClickListener {

            compositeDisposable.add(Completable.create {

                val ci: String = view._editTextCI.text.toString()
                val fv: String = view._editTextFV.text.toString().toUpperCase(Locale.ROOT)
//                if (!Common.isValidFV(fv)) {
//                    Toast.makeText(
//                        context,
//                        "El campo FV no puede estar en blanco",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
                    saveAndSendDataInsertColaborator(ci, fv)
                    dialog.dismiss()

//                }

            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({

                }, {
                    val isValid=Common.isValidFV(view._editTextFV.text.toString().toUpperCase())
                    if(!isValid){
                        showError("El campo FV no puede estar en blanco")
                    }else{
                        it.printStackTrace()
                        showError(context.getString(R.string.error))
                    }

                }))
//            dialog.dismiss()

        }

        view._editTextCI.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {

                view._okButton.isEnabled =
                    Common.isValidCI(view._editTextCI.text.toString().trim(), context)
            }
        })
        view._editTextFV.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
             }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {

                view._okButton.isEnabled =
                    Common.isValidFV(view._editTextCI.text.toString().trim())
            }
        })

        view._okButton.isEnabled =
            Common.isValidCI(view._editTextCI.text.toString().trim(), context)

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveAndSendDataInsertColaborator(
        ci: String,
        fv: String = "00"
    ) {
        Single.create<Pair<Int, String?>> {

            val info = HashMap<String, Any>()
//
//            info.put(Person.KEY_NAME, name)
//            info.put(Person.KEY_LAST_NAME, lastName)

            val person = Person(ci, fv, info)

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
                this["operator"] = PreferencesManager(context).getId()
                this["queue"] = queue.uuid!!
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            val result = APIService.apiService.putCollaborator(
                headers = headerMap,
                data = Gson().toJson(person)
            ).execute()

            if (result.code() == 200) {
                val map = HashMap<String, Any>()
//                map.put(Person.KEY_NAME, name)
//                map.put(Person.KEY_LAST_NAME, lastName)
                val dao = AppDataBase.getInstance(context).dao()

                dao.insertCollaborator(Person(ci, fv, map))
                queue.collaborators.add(ci)
                dao.insertQueue(queue)
            }

            it.onSuccess(
                Pair(result.code(), result.errorBody()?.string() ?: result.message())
            )
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.first == 200) {
                    dialog.dismiss()
                } else {
                    val message = it.second ?: "Error ${it.first}"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                }
                if(it.first==409){
                    Toast.makeText(context, R.string.error_colaborator, Toast.LENGTH_LONG).show()
                }
            }, {
                it.printStackTrace()
                showError(context.getString(R.string.conection_error))

            }).addTo(CompositeDisposable())
    }


}