package cu.control.queue.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.adapters.viewHolders.ViewHolderMyColaborator
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_LAST_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NAME
import cu.control.queue.utils.PreferencesManager

class AdapterMyColaborators :
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

        if (person.info[KEY_NAME] != null || person.info[KEY_LAST_NAME] != null) {
            holder.textViewNombreColaborator.text =
                (person.info[KEY_NAME] as String + " " + person.info[KEY_LAST_NAME])
                        as String? ?: ""
        } else {
            holder.textViewNombreColaborator.text = ""
        }

        holder.textViewCIColaborator.text = "CI: " + person.ci

    }
}