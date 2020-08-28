package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderClient
import cu.control.queue.adapters.viewHolders.ViewHolderQueue
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.utils.Conts.Companion.formatDateBig

class AdapterQueue(
    private val onClickListener: onClickListener
) :
    RecyclerView.Adapter<ViewHolderQueue>() {

    var contentList: List<Queue> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderQueue {
        return ViewHolderQueue(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_queue,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderQueue, position: Int) {
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
        holder.imageOffline.visibility = if (queue.isOffline) View.VISIBLE else View.GONE
        holder.imageSave.visibility = if (queue.isSaved) View.GONE else View.VISIBLE
        holder.textViewName.text = queue.name
        holder.textViewDate.text = formatDateBig.format(queue.startDate)
        holder.textViewID.text = queue.clientsNumber.toString()
        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.imageView.context, R.drawable.ic_recurso_3
            )
        )

        holder.imageDownload.setOnClickListener {
            onClickListener.onDownloadClick(queue)
        }

        holder.imageSave.setOnClickListener {
            onClickListener.onSaveClick(queue)
        }

        holder.layoutBackground.setOnClickListener {
            onClickListener.onClick(queue)
        }

        holder.layoutBackground.setOnLongClickListener {
            onClickListener.onLongClick(it, queue)
            return@setOnLongClickListener true
        }

        holder.textViewReIntents.visibility = View.GONE

        holder.imageDetails.visibility = View.VISIBLE
        holder.imageDetails.setOnClickListener {
            Toast.makeText(
                it.context, if (queue.description.isNullOrBlank())
                    it.context.getText(R.string.notDescroption)
                else
                    queue.description, Toast.LENGTH_LONG
            ).show()
        }
    }
}