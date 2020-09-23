package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderMyColaborator
import cu.control.queue.interfaces.OnColaboratorClickListener
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_LAST_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NAME
import cu.control.queue.utils.PreferencesManager

class AdapterPerson(private val queue: Queue, private val onColaboratorClickListener: OnColaboratorClickListener) :
    RecyclerView.Adapter<ViewHolderMyColaborator>() {

    var contentList: List<Person> = ArrayList()
    private lateinit var context: Context
    private var myCi = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMyColaborator {
        context = parent.context
        myCi = PreferencesManager(context).getCi()
        return ViewHolderMyColaborator(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_my_colaborator,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderMyColaborator, position: Int) {
        val person = contentList[position]

        holder.item_colaborator.background =
            ContextCompat.getDrawable(
                holder.item_colaborator.context,
                when {
                    position % 2 != 0 -> R.drawable.item_white_bg
                    else -> R.drawable.bg_item_dark
                }
            )

        if (person.info[KEY_NAME] != null || person.info[KEY_LAST_NAME] != null) {
            holder.textViewNombreColaborator.text =
                ( person.info[KEY_NAME] as String +" " + person.info[KEY_LAST_NAME])
                        as String? ?: ""
        } else {
            holder.textViewNombreColaborator.text = ""
        }

        holder.textViewCIColaborator.text = person.ci + " - " + person.fv

        holder.item_colaborator.setOnLongClickListener {
            if (person.ci != queue.owner && person.ci != myCi)
                onColaboratorClickListener.onLongClick(it, person)
            return@setOnLongClickListener true
        }

        holder.item_colaborator.setOnClickListener {
            onColaboratorClickListener.onClick(person)
        }
    }
}