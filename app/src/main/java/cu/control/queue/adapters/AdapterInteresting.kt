package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderInteresting
import cu.control.queue.interfaces.OnInterestingClickListener
import cu.control.queue.repository.dataBase.entitys.InterestingClient


class AdapterInteresting(private val onClickListener: OnInterestingClickListener) :
    RecyclerView.Adapter<ViewHolderInteresting>() {

    var contentList: List<InterestingClient> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderInteresting {
        return ViewHolderInteresting(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_interesting,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderInteresting, position: Int) {
        val client = contentList[position]

        holder.item.background =
            ContextCompat.getDrawable(
                holder.item.context,
                when {
                    position % 2 != 0 -> R.drawable.item_white_bg
                    else -> R.drawable.bg_item_dark
                }
            )

        holder.item.setOnClickListener {
            onClickListener.onClick(client)
        }

        holder.clientNumber.text = client.number
        holder.queuename.text = client.queueName
        holder.store.text = client.storeName
        holder.date.text = client.day
        holder.hour.text = client.hour
    }
}