package cu.control.queue.adapters.viewHolders

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product_add_list.view.*

class ViewHolderProductsAdd(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewName: TextView = itemView.textViewProduct
    val checkedView: View = itemView.checkedView
    val itemLayout: LinearLayout = itemView.layoutProduct
}