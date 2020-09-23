package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.item_client.view._circularImageView
import kotlinx.android.synthetic.main.item_client.view._layoutItemPersona
import kotlinx.android.synthetic.main.item_client.view._textViewDate
import kotlinx.android.synthetic.main.item_client.view._textViewNombre
import kotlinx.android.synthetic.main.item_client.view.clientNumber
import kotlinx.android.synthetic.main.item_client.view.imageViewDownloadQueue
import kotlinx.android.synthetic.main.item_client.view.imageViewSaveQueue
import kotlinx.android.synthetic.main.item_queues.view.*

class ViewHolderExportQueues(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewName: TextView = itemView._textViewNombre
    val textViewDate: TextView = itemView._textViewDate
    val layoutBackground: LinearLayout = itemView._layoutItemPersona
    val clientNumberOpenQueue: TextView = itemView.clientNumberOpenQueue
    val clientNumberSave: TextView = itemView.clientNumberSave
    val clientNumberSaveQueue: TextView = itemView.clientNumberSave
    val imageDownload: ImageView = itemView.imageViewDownloadQueue
    val imageSave: ImageView = itemView.imageViewSaveQueue
    val imageViewSaveOpened: ImageView = itemView.imageViewSaveOpened
    val check_export: ImageView = itemView.check_export

}