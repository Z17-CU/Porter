package cu.uci.porter.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import cu.uci.porter.R
import cu.uci.porter.adapters.viewHolders.ViewHolderClient
import cu.uci.porter.fragments.QrReaderFragment
import cu.uci.porter.repository.AppDataBase
import cu.uci.porter.repository.entitys.Queue
import cu.uci.porter.utils.Conts.Companion.formatDateBig
import cu.uci.porter.viewModels.ClientViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import me.yokeyword.fragmentation.SupportFragment

class AdapterQueue(private val context: SupportFragment, private val viewModel: ClientViewModel) :
    RecyclerView.Adapter<ViewHolderClient>() {

    var contentList: List<Queue> = ArrayList()

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
        val queue = contentList[position]

        holder.layoutBackground.background =
            ContextCompat.getDrawable(
                holder.layoutBackground.context,
                if (position % 2 != 0) R.drawable.item_white_bg else R.drawable.bg_item_dark
            )

        holder.textViewName.text = queue.name
        holder.textViewDate.text = formatDateBig.format(queue.startDate)
        holder.textViewID.text = queue.clientsNumber.toString()
        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.imageView.context, R.drawable.ic_recurso_3
            )
        )

        holder.layoutBackground.setOnClickListener {
            showReaderOptions(queue)
        }

        holder.layoutBackground.setOnLongClickListener {
            showPopup(it, position)
            return@setOnLongClickListener true
        }

        holder.layoutmarker.visibility = View.GONE
        holder.textViewReIntents.visibility = View.GONE
    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(view: View, position: Int) {
        val context = view.context

        val popupMenu = PopupMenu(context!!, view)
        (context as Activity).menuInflater.inflate(R.menu.menu_delete, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialog.Builder(context)
                        .setTitle("Eliminar")
                        .setMessage("¿Desea eliminar " + contentList[position].name + " de la lista?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            Completable.create {
                                AppDataBase.getInstance(context).dao()
                                    .deleteQueue(contentList[position])
                                it.onComplete()
                            }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe()

                        }
                        .create()
                        .show()
                }
            }
            false
        }
        val wrapper = ContextThemeWrapper(context, R.style.PopupWhite)
        val menuPopupHelper =
            MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, view)
        menuPopupHelper.setForceShowIcon(true)
        menuPopupHelper.show()
    }

    private fun showReaderOptions(queue: Queue) {
        val bottomSheetDialog = BottomSheetDialog(context.requireContext())
        bottomSheetDialog.setContentView(R.layout.layout_buttom_sheet_dialog_open_reader)
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<TextView>(R.id._optionAsChecker)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            context.start(QrReaderFragment(queue, viewModel, true))
        }

        bottomSheetDialog.findViewById<TextView>(R.id._optionAsReader)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            context.start(QrReaderFragment(queue, viewModel, false))
        }
    }
}