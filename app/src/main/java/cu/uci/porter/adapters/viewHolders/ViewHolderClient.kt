package cu.uci.porter.adapters.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_client.view.*

class ViewHolderClient(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView._circularImageView
    val textViewName = itemView._textViewNombre
    val textViewID = itemView._textViewCI
    val textViewDate = itemView._textViewDate
    val layoutmarker = itemView._markerLayout
    val layoutBackground = itemView._layoutItemPersona
}