package cu.control.queue.utils.permissions


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.annotation.DrawableRes
import cu.control.queue.R


object RationaleDialog {

    @SuppressLint("InflateParams")
    fun createFor(context: Context, message: String, @DrawableRes vararg drawables: Int): AlertDialog.Builder {
        val view = LayoutInflater.from(context).inflate(R.layout.permissions_rationale_dialog, null)
        val header = view.findViewById<ViewGroup>(R.id.header_container)
        val text = view.findViewById<TextView>(R.id.message)

        for (i in drawables.indices) {
            val imageView = ImageView(context)
            imageView.setImageDrawable(context.resources.getDrawable(drawables[i]))
            imageView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

            header.addView(imageView)

            if (i != drawables.size - 1) {
                val plus = TextView(context)
                plus.text = "+"
                plus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
                plus.setTextColor(Color.WHITE)

                val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(dip2px(context, 20f), 0, dip2px(context, 20f), 0)

                plus.layoutParams = layoutParams
                header.addView(plus)
            }
        }

        text.text = message

        return AlertDialog.Builder(context, R.style.RationaleDialog).setView(view)
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
