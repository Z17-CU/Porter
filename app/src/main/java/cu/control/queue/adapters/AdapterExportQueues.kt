package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderExportQueues
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.utils.Conts.Companion.formatDateBig

class AdapterExportQueues :
    RecyclerView.Adapter<ViewHolderExportQueues>() {

    var contentList: List<Queue> = ArrayList()
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

        holder.layoutBackground.setOnClickListener {
            queue.checked = queue.checked != true
            updateItem(holder, queue)
        }
        updateItem(holder, queue)
    }

    private fun updateItem(holder: ViewHolderExportQueues, queue: Queue){
        if (queue.checked == true) {
            holder.check_export.visibility = View.VISIBLE
            holder.clientNumberOpenQueue.visibility = View.GONE
            holder.clientNumberSaveQueue.visibility = View.GONE
        } else {
            if (queue.isSaved) {
                holder.clientNumberOpenQueue.visibility = View.GONE
                holder.clientNumberSaveQueue.visibility = View.VISIBLE
                holder.clientNumberSaveQueue.text = queue.clientsNumber.toString()
            } else {
                holder.clientNumberOpenQueue.visibility = View.VISIBLE
                holder.clientNumberSaveQueue.visibility = View.GONE
                holder.clientNumberOpenQueue.text = queue.clientsNumber.toString()
            }
            holder.check_export.visibility = View.GONE
        }
    }
}