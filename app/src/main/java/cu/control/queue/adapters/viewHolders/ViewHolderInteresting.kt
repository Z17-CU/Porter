package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_interesting.view.*

class ViewHolderInteresting(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val clientNumber: TextView = itemView.clientNumber
    val queuename: TextView = itemView._queueName
    val store: TextView = itemView._store
    val date: TextView = itemView._textViewDate
    val item: LinearLayout = itemView._item_interesting
}