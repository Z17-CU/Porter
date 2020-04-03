package cu.uci.porter.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import cu.uci.porter.R

class Progress @SuppressLint("InflateParams")
constructor(val context: Context) {

    private var view: View
    private lateinit var builder: AlertDialog.Builder
    private var dialog: Dialog? = null
    private var isShowing = false

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        try {
            view = inflater.inflate(R.layout.progress, null)
            init()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                view = inflater.inflate(R.layout.progress_respaldo, null)
                init()
            } catch (e: Exception) {
                view = View(context)
                e.printStackTrace()
            }
        }
    }

    private fun init() {
        builder = AlertDialog.Builder(view.context)
    }

    fun show() {
        if (!isShowing) {
            isShowing = true
            if (dialog == null) {
                try {
                    builder.setView(view)
                    dialog = builder.create()
                    dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog?.setCancelable(false)
                    dialog?.show()
                    dialog?.window?.setLayout(
                        context.resources.getDimension(R.dimen.progressSize).toInt(),
                        context.resources.getDimension(R.dimen.progressSize).toInt()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                dialog!!.show()
            }
        }
    }

    fun dismiss() {
        isShowing = false
        try {
            dialog?.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}