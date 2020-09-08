package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.item_client.view._circularImageView
import kotlinx.android.synthetic.main.item_client.view._imageViewCheck
import kotlinx.android.synthetic.main.item_client.view._layoutItemPersona
import kotlinx.android.synthetic.main.item_client.view._textViewReIntents
import kotlinx.android.synthetic.main.item_client.view.clientNumber
import kotlinx.android.synthetic.main.item_client.view.imageViewDetails
import kotlinx.android.synthetic.main.item_client.view.imageViewDownloadQueue
import kotlinx.android.synthetic.main.item_client.view.imageViewOffline
import kotlinx.android.synthetic.main.item_client.view.imageViewOwner
import kotlinx.android.synthetic.main.item_client.view.imageViewSaveQueue
import kotlinx.android.synthetic.main.item_search_filter.view.*

class ViewHolderFilterSearch(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: CircularImageView = itemView._circularImageView
    val textViewNameQueue: TextView = itemView._textViewNameQueue
    val textViewCreateDate: TextView = itemView._textViewCreateDate
    val textViewUpdateDate: TextView = itemView._textViewUpdateDate
    val textViewDeleteDate: TextView = itemView._textViewDeleteDate
    val textViewDateCreate: TextView = itemView._textViewDateCreate
    val textViewDateUpdate: TextView = itemView._textViewDateUpdate
    val textViewDateDelete: TextView = itemView._textViewDateDelete
    val layoutBackground: LinearLayout = itemView._layoutItemPersona
    val textViewReIntents: TextView = itemView._textViewReIntents
    val clientNumber: TextView = itemView.clientNumber
    val imageViewCheck: ImageView = itemView._imageViewCheck
    val imageDetails: ImageView = itemView.imageViewDetails
    val imageDownload: ImageView = itemView.imageViewDownloadQueue
    val imageSave: ImageView = itemView.imageViewSaveQueue
    val imageOwner: ImageView = itemView.imageViewOwner
    val imageOffline: ImageView = itemView.imageViewOffline
}