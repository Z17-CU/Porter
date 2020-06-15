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
    var checkMode = true
    var done: Boolean = false

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
                when {
                    client.searched -> R.drawable.item_accent_bg
                    client.selected && done -> {
                        R.drawable.item_green_bg
                    }
                    client.selected && !done -> {
                        R.drawable.item_red_bg
                    }
                    checkMode && position % 2 != 0 -> R.drawable.item_blue_bg
                    checkMode && position % 2 == 0 -> R.drawable.bg_item_dark_blue
                    position % 2 != 0 -> R.drawable.item_white_bg
                    else -> R.drawable.bg_item_dark
                }
            )


        when {
            !client.isChecked -> {
                holder.clientNumber.visibility = View.VISIBLE
                holder.imageViewCheck.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
                holder.imageView.background = ContextCompat.getDrawable(
                    holder.imageView.context,
                    R.drawable.round_accent_bg
                )
                holder.imageView.setImageDrawable(null)

                holder.clientNumber.text = client.number.toString()
            }
            client.isChecked -> {
                holder.clientNumber.visibility = View.GONE
                holder.imageView.visibility = View.GONE
                holder.imageViewCheck.visibility = View.VISIBLE
            }
        }

        holder.textViewName.text = client.name
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