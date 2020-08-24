package cu.control.queue.dialogs

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.zxing.Result
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.interfaces.OnDialogHiClientEvent
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import cu.control.queue.utils.PreferencesManager
import cu.control.queue.utils.permissions.Permissions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_dialog_hi_client.view.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class DialogAddCollaborator(
    private val context: Context,
    private val queue: Queue
) : ZXingScannerView.ResultHandler, OnDialogHiClientEvent {

    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    fun create(): AlertDialog {
        dialog = AlertDialog.Builder(context)
            .setView(getView())
            .create()

        startReader()

        return dialog
    }

    @SuppressLint("LogNotTimber")
    private fun getView(): View {

        view = View.inflate(context, R.layout.layout_dialog_hi_client, null)

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveAndSendData(name: String, lastName: String, ci: String, fv: String = "00") {
        Single.create<Pair<Int, String?>> {

            val info = HashMap<String, Any>()

            info.put(Person.KEY_NAME, name)
            info.put(Person.KEY_LAST_NAME, lastName)

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
                map.put(Person.KEY_NAME, name)
                map.put(Person.KEY_LAST_NAME, lastName)
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
                    stopReader()
                    dialog.dismiss()
                } else {
                    val message = it.second ?: "Error ${it.first}"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    startReader()
                }
            }, {
                it.printStackTrace()
                showError(context.getString(R.string.conection_error))
                startReader()
            }).addTo(CompositeDisposable())
    }

    private fun startReader() {

        Permissions.with(context as Activity)
            .request(Manifest.permission.CAMERA)
            .ifNecessary()
            .onAllGranted {
                view._zXingScannerView.stopCamera()
                view._zXingScannerView.setResultHandler(this)
                view._zXingScannerView.startCamera()
            }
            .execute()
    }

    private fun stopReader() {
        view._zXingScannerView.stopCamera()
    }

    override fun handleResult(result: Result) {
        //play sound
        val mp = MediaPlayer.create(context, R.raw.beep)
        mp.start()
        //vibrate
        val vibratorService = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(120)

        val client = Common.stringToPorterHistruct(result)
        client?.let {
            saveAndSendData(it.name, it.last_name, it.ci, it.fv)
            return
        }
        showError(context.getString(R.string.readWrong))
        startReader()
    }

    override fun onCameraPermissionOk() {
        startReader()
    }

    override fun onResume() {
        if (view._qrView.visibility == View.VISIBLE)
            startReader()
    }

    override fun onPause() {
        stopReader()
    }
}