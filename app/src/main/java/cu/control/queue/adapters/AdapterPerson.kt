package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import cu.control.queue.BuildConfig
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderQueue
import cu.control.queue.interfaces.OnColaboratorClickListener
import cu.control.queue.repository.dataBase.AppDataBase
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_LAST_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NAME
import cu.control.queue.repository.retrofit.APIService
import cu.control.queue.utils.PreferencesManager
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class AdapterPerson(private val queue: Queue, private val onColaboratorClickListener: OnColaboratorClickListener) :
    RecyclerView.Adapter<ViewHolderQueue>() {

    var contentList: List<Person> = ArrayList()
    private lateinit var context: Context
    private var myCi = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderQueue {
        context = parent.context
        myCi = PreferencesManager(context).getCi()
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
        val person = contentList[position]

        holder.layoutBackground.background =
            ContextCompat.getDrawable(
                holder.layoutBackground.context,
                when {
                    position % 2 != 0 -> R.drawable.item_white_bg
                    else -> R.drawable.bg_item_dark
                }
            )

        holder.clientNumber.visibility = View.VISIBLE
        holder.imageViewCheck.visibility = View.GONE
        holder.imageView.visibility = View.VISIBLE
        holder.imageView.background = ContextCompat.getDrawable(
            holder.imageView.context,
            R.drawable.round_accent_bg
        )
        holder.imageView.setImageDrawable(null)

        holder.clientNumber.text = "C"
        if (person.info[KEY_NAME] != null || person.info[KEY_LAST_NAME] != null) {
            holder.textViewName.text =
                ( person.info[KEY_NAME] as String +" " + person.info[KEY_LAST_NAME])
                        as String? ?: ""
        } else {
            holder.textViewName.text = ""
        }

        holder.textViewID.text = person.ci + " - " + person.fv
        holder.textViewDate.text = ""
        holder.textViewReIntents.visibility = View.GONE

        holder.imageOwner.visibility =
            if (person.ci == queue.owner) View.VISIBLE else View.GONE

        holder.layoutBackground.setOnLongClickListener {
            if (person.ci != queue.owner && person.ci != myCi)
                onColaboratorClickListener.onLongClick(it, person)
            return@setOnLongClickListener true
        }

        holder.layoutBackground.setOnClickListener {
            onColaboratorClickListener.onClick(person)
        }
    }
}