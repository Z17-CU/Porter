package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderFilterSearch
import cu.control.queue.interfaces.onClickListener
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.utils.Conts.Companion.formatDateBigNatural

class AdapterQueueFilterSearch(
    private val onClickListener: onClickListener
) :
    RecyclerView.Adapter<ViewHolderFilterSearch>() {

    var contentList: List<Queue> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFilterSearch {
        return ViewHolderFilterSearch(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_search_filter,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderFilterSearch, position: Int) {
        val queue = contentList[position]



        holder.imageDownload.visibility = if (queue.downloaded) View.GONE else View.VISIBLE
        holder.imageOffline.visibility = if (queue.isOffline) View.VISIBLE else View.GONE
        holder.imageSave.visibility = if (queue.isSaved) View.GONE else View.VISIBLE
        holder.textViewNameQueue.text = queue.name

        holder.textViewDateCreate.text = formatDateBigNatural.format(queue.startDate)
        holder.textViewDateUpdate.text = formatDateBigNatural.format(queue.updated_date)
        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.imageView.context, R.drawable.ic_launcher_foreground
            )
        )

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

        holder.textViewReIntents.visibility = View.GONE

        holder.imageDetails.visibility = View.VISIBLE
        holder.imageDetails.setOnClickListener {
            Toast.makeText(
                it.context, if (queue.description.isBlank())
                    it.context.getText(R.string.notDescroption)
                else
                    queue.description, Toast.LENGTH_LONG
            ).show()
        }
    }
}