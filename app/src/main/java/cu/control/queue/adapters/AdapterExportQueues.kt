package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderExportQueues
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.utils.Conts.Companion.formatDateBig

class AdapterExportQueues(
    private val onClickListener: onClickListener
) :
    RecyclerView.Adapter<ViewHolderExportQueues>() {

    var contentList: List<Queue> = ArrayList()
    val exportList = mutableListOf<Queue>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderExportQueues {
        return ViewHolderExportQueues(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_queues,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderExportQueues, position: Int) {
        val queue = contentList[position]

        holder.imageDownload.visibility = if (queue.downloaded) View.GONE else View.VISIBLE
        holder.imageSave.visibility = if (queue.isSaved) View.GONE else View.VISIBLE
        holder.imageViewSaveOpened.visibility = View.GONE
        holder.clientNumberOpenQueue.visibility = View.GONE
        holder.textViewName.text = queue.name
        holder.textViewDate.text = formatDateBig.format(queue.startDate)

        var export = false
        holder.layoutBackground.setOnClickListener {
            if (!export) {
                holder.check_export.visibility = View.VISIBLE
                holder.clientNumberSave.visibility = View.GONE
                export = true
                exportList.add(queue)
            } else {
                holder.check_export.visibility = View.GONE
                holder.clientNumberSave.visibility = View.VISIBLE
                export = false
//todo ver funcionalidad
                if (exportList.size > 0) {
                    exportList.removeAt(position - 1)
                } else {
                    exportList.removeAt(0)
                }

            }
        }

        onClickListener.onClickExport(exportList)
        if (queue.isSaved) {

            holder.clientNumberOpenQueue.visibility = View.GONE
            holder.clientNumberSaveQueue.visibility = View.VISIBLE
            holder.clientNumberSaveQueue.text = queue.clientsNumber.toString()

        }

    }
}