package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.item_client.view.*
import kotlinx.android.synthetic.main.item_client.view._layoutItemPersona
import kotlinx.android.synthetic.main.item_queue.view.*

class ViewHolderQueue(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val imageView: CircularImageView = itemView._circularImageView
    val textViewStatusQueue: TextView = itemView._textViewStatusQueue
    val textNameQueue: TextView = itemView._textNameQueue
    val textviewCountClient: TextView = itemView.textviewCountClient
    val layoutBackground: LinearLayout = itemView._layoutItemPersona
    val card_view: CardView = itemView.card_view
//    val textViewReIntents: TextView = itemView._textViewReIntents
//    val clientNumber: TextView = itemView.clientNumber
//    val imageViewCheck: ImageView = itemView._imageViewCheck
//    val imageDetails: ImageView = itemView.imageViewDetails
}