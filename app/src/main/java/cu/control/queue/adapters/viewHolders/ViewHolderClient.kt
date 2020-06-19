package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.item_client.view.*

class ViewHolderClient(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: CircularImageView = itemView._circularImageView
    val textViewName: TextView = itemView._textViewNombre
    val textViewID: TextView = itemView._textViewCI
    val textViewDate: TextView = itemView._textViewDate
    val layoutmarker: LinearLayout = itemView._markerLayout
    val layoutBackground: LinearLayout = itemView._layoutItemPersona
    val textViewReIntents: TextView = itemView._textViewReIntents
    val clientNumber: TextView = itemView.clientNumber
    val imageViewCheck: ImageView = itemView._imageViewCheck
    val imageDetails: ImageView = itemView.imageViewDetails
}