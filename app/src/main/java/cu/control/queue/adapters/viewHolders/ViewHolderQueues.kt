package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_queues.view.*
import kotlinx.android.synthetic.main.item_queues.view._layoutItemPersona
import kotlinx.android.synthetic.main.item_queues.view._textViewDate
import kotlinx.android.synthetic.main.item_queues.view._textViewNombre
import kotlinx.android.synthetic.main.item_queues.view.imageViewDownloadQueue
import kotlinx.android.synthetic.main.item_queues.view.imageViewSaveQueue

class ViewHolderQueues(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val imageView: CircularImageView = itemView._circularImageView
    val textViewName: TextView = itemView._textViewNombre
    val textViewDate: TextView = itemView._textViewDate
    val layoutBackground: LinearLayout = itemView._layoutItemPersona
    val clientNumberOpenQueue: TextView = itemView.clientNumberOpenQueue
    val clientNumberSaveQueue: TextView = itemView.clientNumberSave
    val imageDownload: ImageView = itemView.imageViewDownloadQueue
    val imageSave: ImageView = itemView.imageViewSaveQueue
    val imageViewSaveOpened: ImageView = itemView.imageViewSaveOpened

    val itemQueue: LinearLayout = itemView.viewItem
    val itemSeparator: LinearLayout = itemView.viewSeparator
    val textViewSeparator: TextView = itemView.textViewSeparator
}