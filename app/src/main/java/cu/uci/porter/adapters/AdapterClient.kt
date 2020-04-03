package cu.uci.porter.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cu.uci.porter.R
import cu.uci.porter.adapters.viewHolders.ViewHolderClient
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.utils.Conts.Companion.formatDateOnlyTime

class AdapterClient : RecyclerView.Adapter<ViewHolderClient>() {

    var contentList: List<Client> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClient {
        return ViewHolderClient(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_client,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderClient, position: Int) {
        val client = contentList[position]

        holder.layoutBackground.background =
            ContextCompat.getDrawable(
                holder.layoutBackground.context,
                if (position % 2 != 0) R.drawable.item_white_bg else R.drawable.bg_item_dark
            )

        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.imageView.context,
                if (client.sex == Client.SEX_WOMAN) R.drawable.ic_girl_big else R.drawable.ic_man_big
            )
        )

        holder.textViewName.text = "${client.name} ${client.lastName}"
        holder.textViewID.text = client.ci
        holder.textViewDate.text = formatDateOnlyTime.format(client.lastRegistry)
        holder.textViewReIntents.visibility = if (client.reIntent > 0) {
            holder.textViewReIntents.text = if (client.reIntent > 9) {
                "+9"
            } else {
                client.reIntent.toString()
            }
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.layoutmarker.setBackgroundColor(
            when {
                client.age < 12 -> {
                    ContextCompat.getColor(holder.layoutmarker.context, R.color.google_blue)
                }
                client.age in 12..30 -> {
                    ContextCompat.getColor(holder.layoutmarker.context, R.color.google_green)
                }
                client.age in 31..55 -> {
                    ContextCompat.getColor(holder.layoutmarker.context, R.color.google_yellow)
                }
                else -> {
                    ContextCompat.getColor(holder.layoutmarker.context, R.color.google_red)
                }
            }
        )
    }
}