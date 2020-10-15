package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderProductsAdd
import cu.control.queue.repository.dataBase.entitys.Product
import cu.control.queue.utils.ColorGenerator


class AdapterProducstToAdd :
    RecyclerView.Adapter<ViewHolderProductsAdd>() {

    var contentList: ArrayList<Product> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderProductsAdd {
        return ViewHolderProductsAdd(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_product_add_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderProductsAdd, position: Int) {
        val product = contentList[position]

        holder.textViewName.text = product.name
        val color = ColorGenerator.MATERIAL.getColor(product.name)
        holder.textViewName.setTextColor(color)
        if (product.ischeck) {
            holder.checkedView.visibility = View.VISIBLE
            holder.checkedView.background = drawCircle(color)
        } else {
            holder.checkedView.visibility = View.GONE
        }
        holder.itemLayout.setOnClickListener {
            contentList[position].ischeck = !product.ischeck
            notifyDataSetChanged()
        }
        holder.itemLayout.setOnLongClickListener {
            AlertDialog.Builder(it.context,R.style.RationaleDialog)
                .setTitle("Eliminar producto")
                .setMessage("¿Desea eliminar ${product.name} la lista general?")
                .setPositiveButton(it.context.getText(R.string.eliminar)) { _, _ ->
                    contentList.remove(product)
                    notifyDataSetChanged()
                }
                .setNegativeButton(it.context.getString(android.R.string.cancel), null)
                .create().show()
            true
        }
    }

    private fun drawCircle(backgroundColor: Int): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        shape.setColor(backgroundColor)
        return shape
    }
}