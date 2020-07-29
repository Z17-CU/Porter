package cu.control.queue.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import cu.control.queue.R
import cu.control.queue.interfaces.onSave
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.utils.Common
import cu.control.queue.utils.Common.Companion.isValidCI
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view.*

class DialogInsertClient(
    private val context: Context,
    private val compositeDisposable: CompositeDisposable,
    private val onSave: onSave
) {

    private lateinit var dao: Dao
    private lateinit var dialog: AlertDialog

    fun create(): AlertDialog {
        dao = AppDataBase.getInstance(context).dao()
        dialog = AlertDialog.Builder(context)
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

                val client = Client(
                    view._editTextCI.text.toString().trim(),
                    view._editTextCI.text.trim().toString().toLong(),
                    view._editTextCI.text.toString().trim(),
                    null,
                    Common.getSex(view._editTextCI.text.toString().trim()),
                    Common.getAge(view._editTextCI.text.toString().trim())
                )

                onSave.save(client)

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

        view._editTextCI.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {

                view._okButton.isEnabled = isValidCI(view._editTextCI.text.toString().trim(), context)
            }
        })

        view._okButton.isEnabled = isValidCI(view._editTextCI.text.toString().trim(), context)

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
}