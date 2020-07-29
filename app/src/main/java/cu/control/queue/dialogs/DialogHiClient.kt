package cu.control.queue.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import cu.control.queue.utils.PreferencesManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_dialog_hi_client.view.*
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view._okButton
import kotlinx.android.synthetic.main.layout_dialog_insert_queue.view._editTextName

class DialogHiClient(
    private val context: Context,
    private val compositeDisposable: CompositeDisposable,
    private val preferences: PreferencesManager
) {

    private lateinit var dialog: AlertDialog
    private lateinit var view: View
    private val textWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable) {

            val nameOk = view._editTextName.text.trim().isNotEmpty()
            val lastNameOk = view._editTextLastName.text.trim().isNotEmpty()
            val ciOk = Common.isValidCI(view._editTextCI.text.trim().toString(), context)

            view._okButton.isEnabled = nameOk && lastNameOk && ciOk

            view.textViewRequeridoName.visibility = if (nameOk) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
            view.textViewRequeridoLastName.visibility = if (lastNameOk) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
            view.textViewRequeridoCI.visibility = if (ciOk) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }
    }

    fun create(): AlertDialog {
        dialog = AlertDialog.Builder(context)
            .setView(getView())
            .setCancelable(false)
            .create()

        return dialog
    }

    @SuppressLint("LogNotTimber")
    private fun getView(): View {

        view = View.inflate(context, R.layout.layout_dialog_hi_client, null)

        view._okButton.setOnClickListener {

            saveAndSendData(
                view._editTextName.text.toString().trim(),
                view._editTextLastName.text.toString().trim(),
                view._editTextCI.text.toString().trim()
            )
        }

        view._editTextName.addTextChangedListener(textWatcher)
        view._editTextLastName.addTextChangedListener(textWatcher)
        view._editTextCI.addTextChangedListener(textWatcher)

        view._okButton.isEnabled = false

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveAndSendData(name: String, lastName: String, ci: String, fv: String = "00") {
        compositeDisposable.add(Single.create<Pair<Int, String?>> {

            preferences.setName(name)
            preferences.setLastName(lastName)
            preferences.setCI(ci)
            preferences.setFV(fv)

            val struct = PorterHistruct(name, lastName, ci, fv)

            val data = Common.porterHiToString(struct)

            val headerMap = mutableMapOf<String, String>().apply {
                this["Content-Type"] = "application/json"
                this["Authorization"] = Base64.encodeToString(
                    BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                ) ?: ""
            }

            val result = APIService.apiService.hiPorter(
                data = data, headers = headerMap
            ).execute()
            it.onSuccess(
                Pair(result.code(), result.message())
            )
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.first == 200) {
                    dialog.dismiss()
                } else
                    showError(it.second ?: "Error ${it.first}")
            }, {
                it.printStackTrace()
                showError(context.getString(R.string.conection_error))
            }))
    }
}