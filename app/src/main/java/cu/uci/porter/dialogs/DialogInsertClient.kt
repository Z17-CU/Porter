package cu.uci.porter.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import cu.uci.porter.R
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.Dao
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.utils.Common
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_dialog_insert_client.view.*
import java.util.*

class DialogInsertClient(
    private val context: Context,
    private val compositeDisposable: CompositeDisposable,
    private val queueId: Int
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

        var isNameOK = false
        var isLastNameOK = false
        var isCIOk = false

        val view = View.inflate(context, R.layout.layout_dialog_insert_client, null)

        view._cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        view._okButton.setOnClickListener {

            compositeDisposable.add(Completable.create {

                val client = Client(
                    view._editTextName.text.toString().trim(),
                    view._editTextLastName.text.toString().trim(),
                    view._editTextCI.text.trim().toString().toLong(),
                    view._editTextCI.text.toString().trim(),
                    null,
                    Common.getSex(view._editTextCI.text.toString().trim()),
                    Common.getAge(view._editTextCI.text.toString().trim()),
                    Calendar.getInstance().timeInMillis,
                    queueId
                )

                if (dao.clientExist(client.id) > 0) {
                    showError(context.getString(R.string.clientExist))
                } else {
                    dao.insertClient(client)
                }

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

        view._editTextName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                isNameOK = s.trim().isNotEmpty()

                view._okButton.isEnabled = isCIOk && isLastNameOK && isNameOK
            }
        })

        view._editTextLastName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                isLastNameOK = s.trim().isNotEmpty()

                view._okButton.isEnabled = isCIOk && isLastNameOK && isNameOK
            }
        })

        view._editTextCI.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                isCIOk = s.length == 11

                view._okButton.isEnabled = isCIOk && isLastNameOK && isNameOK
            }
        })

        view._okButton.isEnabled = isCIOk && isLastNameOK && isNameOK

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
}