package cu.control.queue.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import cu.control.queue.R
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.Dao
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamCreateQueue
import cu.control.queue.repository.dataBase.entitys.payload.params.ParamUpdateQueue
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
import kotlinx.android.synthetic.main.layout_dialog_insert_queue.view.*
import java.util.*
import kotlin.collections.HashMap

class DialogCreateQueue(
    private val context: Context,
    private val compositeDisposable: CompositeDisposable,
    private val id: Long = -1L,
    private val clientViewModel: ClientViewModel
) {

    private lateinit var dao: Dao
    private lateinit var dialog: AlertDialog
    private var queue: Queue? = null

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

        val view = View.inflate(context, R.layout.layout_dialog_insert_queue, null)

        view._cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        view._okButton.setOnClickListener {

            compositeDisposable.add(Completable.create {

                val time = Calendar.getInstance().timeInMillis

                val thisqueue = if (queue == null) {

                    Queue(
                        time,
                        view._editTextName.text.toString().trim(),
                        Calendar.getInstance().timeInMillis,
                        description = view._editTextDescription.text.toString().trim(),
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
                    queue!!.name = view._editTextName.text.toString().trim()
                    queue!!.description = view._editTextDescription.text.toString().trim()
                    queue!!
                }
                dao.insertQueue(thisqueue)

                var tag = ""
                val map = mutableMapOf<String, String>()
                map[Param.KEY_QUEUE_NAME] = thisqueue.name
                map[Param.KEY_QUEUE_DESCRIPTION] = thisqueue.description
                val param = if (queue == null) {
                    tag = Param.TAG_CREATE_QUEUE
                    ParamCreateQueue(thisqueue.business ?: 1, map, thisqueue.created_date ?: time)
                } else {
                    tag = Param.TAG_UPDATE_QUEUE
                    ParamUpdateQueue(map, thisqueue.updated_date ?: time)
                }

                clientViewModel.onRegistreAction(thisqueue.uuid ?: "", param, tag, context)

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

                view._okButton.isEnabled = s.toString().trim().isNotEmpty()

                view.textViewRequerido.visibility = if (view._okButton.isEnabled) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            }
        })

        view._okButton.isEnabled = view._editTextName.text.toString().trim().isNotEmpty()

        if (id != -1L) {
            Single.create<Queue> {
                queue = dao.getQueue(id)
                it.onSuccess(queue!!)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { queue ->
                    view._editTextName.setText(queue.name)
                    view._editTextDescription.setText(queue.description)
                    view._okButton.setText(context.getString(R.string.editar))
                }.addTo(compositeDisposable)
        }

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
}