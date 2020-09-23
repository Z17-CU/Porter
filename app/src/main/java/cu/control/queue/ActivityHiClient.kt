package cu.control.queue

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import cu.control.queue.interfaces.OnDialogHiClientEvent
import cu.control.queue.repository.dataBase.entitys.PorterHistruct
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.Common
import cu.control.queue.utils.PreferencesManager
import cu.control.queue.utils.permissions.Permissions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_comment_validate.view.*
import kotlinx.android.synthetic.main.layout_dialog_hi_client.*
import kotlinx.android.synthetic.main.layout_server_message.view.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ActivityHiClient : AppCompatActivity(), ZXingScannerView.ResultHandler,
    OnDialogHiClientEvent {

    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var preferences: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferencesManager(this)
        if (preferences.isFirstRun()) {
            compositeDisposable = CompositeDisposable()
            setContentView(R.layout.activity_hi_client)
            dialogInfo(this)

            val fab: View = findViewById(R.id.fab)
            fab.setOnClickListener {
                dialogInfo(this)
            }
        } else {
            goToMain()
        }
    }

    private fun dialogInfo(context: Context) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_comment_validate, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .show()
        dialogView.buttonOk.setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun startReader() {

        Permissions.with(this)
            .request(Manifest.permission.CAMERA)
            .ifNecessary()
            .onAllGranted {
                _zXingScannerView.stopCamera()
                _zXingScannerView.setResultHandler(this)
                _zXingScannerView.startCamera()
            }
            .execute()
    }

    private fun stopReader() {
        _zXingScannerView.stopCamera()
    }

    override fun handleResult(result: Result) {
        //play sound
        val mp = MediaPlayer.create(this, R.raw.beep)
        mp.start()
        //vibrate
        val vibratorService = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(120)

        val client = Common.stringToPorterHistruct(result, this)
        client?.let {
            saveAndSendData(it.name, it.last_name, it.ci, it.fv,PreferencesManager(this@ActivityHiClient).getStoreVersion())
            goToMain()
            return
        }
        showError("Lectura incorrecta")
        startReader()
    }

    override fun onCameraPermissionOk() {
        startReader()
    }

    override fun onResume() {
        super.onResume()
        if (_qrView.visibility == View.VISIBLE)
            startReader()
    }

    override fun onPause() {
        super.onPause()

        stopReader()
    }


    private fun showError(error: String) {
        (this).runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveAndSendData(
        name: String,
        lastName: String,
        ci: String,
        fv: String = "00",
        storeVersion: Int,
        store:String=""

    ) {
        compositeDisposable.add(Single.create<Pair<Int, String?>> {

            preferences.setName(name)
            preferences.setLastName(lastName)
            preferences.setCI(ci)
            preferences.setFV(fv)
            preferences.setStoreVersion(storeVersion)

            val struct = PorterHistruct(name, lastName, ci, fv,storeVersion)

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
                Pair(result.code(), result.errorBody()?.string() ?: result.message())
            )
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.first == 200) {
                    stopReader()

                } else {
                    val message = it.second ?: "Error ${it.first}"
                    val inflater =
                        this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.layout_server_message, null)
                    view._textViewMessage.text = message
                    AlertDialog.Builder(this)
                        .setView(view)
                        .setOnDismissListener {
                            startReader()
                        }
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show()
                }
            }, {
                it.printStackTrace()
                showError(this.getString(R.string.conection_error))
                startReader()
            }))
    }

    private fun goToMain() {
        preferences.setFirstRun()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}