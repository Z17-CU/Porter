package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderQueues
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.utils.Conts.Companion.formatDateBig

class AdapterQueueOpen(
    private val onClickListener: onClickListener
) :
    RecyclerView.Adapter<ViewHolderQueues>() {

    var contentList: List<Queue> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderQueues {
        return ViewHolderQueues(
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
    override fun onBindViewHolder(holder: ViewHolderQueues, position: Int) {
        val queue = contentList[position]

        holder.layoutBackground.background =
            ContextCompat.getDrawable(
                holder.layoutBackground.context,
                when {
                    queue.isOffline -> R.drawable.item_offline_bg
                    position % 2 != 0 -> R.drawable.item_white_bg
                    else -> R.drawable.bg_item_dark
                }
            )

        holder.imageDownload.visibility = if (queue.downloaded) View.GONE else View.VISIBLE
        holder.imageSave.visibility = if (queue.isSaved) View.GONE else View.VISIBLE
        holder.imageViewSaveOpened.visibility = if (!queue.isSaved) View.GONE else View.VISIBLE
        holder.textViewName.text = queue.name

        if (queue.isSaved) {

            holder.clientNumberOpenQueue.visibility = View.GONE
            holder.clientNumberSaveQueue.visibility = View.VISIBLE
            holder.clientNumberSaveQueue.text = queue.clientsNumber.toString()

        }

        holder.textViewDate.text = formatDateBig.format(queue.startDate)
        holder.imageDownload.setOnClickListener {
            onClickListener.onDownloadClick(queue)
        }

        holder.imageSave.setOnClickListener {
            onClickListener.onSaveClick(queue, false)
        }

        holder.layoutBackground.setOnClickListener {
            onClickListener.onClick(queue)
        }

        holder.layoutBackground.setOnLongClickListener {
            onClickListener.onLongClick(it, queue)
            return@setOnLongClickListener true
        }

//        holder.textViewReIntents.visibility = View.GONE


    }
}