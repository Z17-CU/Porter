package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_my_colaborator.view.*

class ViewHolderMyColaborator(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textViewNombreColaborator: TextView = itemView._textViewNombreColaborator

    val textViewCIColaborator: TextView = itemView._textViewCIColaborator

}