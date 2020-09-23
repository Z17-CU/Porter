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

class AdapterMyColaborators() :
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

        holder.item_colaborator.setOnClickListener {
            var active = false

            active = if (!active) {
                holder.item_colaborator.setBackgroundColor(context.resources.getColor(R.color.blue_drawer))
                true


            } else {
//                holder.item_colaborator.setBackgroundColor(context.resources.getColor(R.color.white))
                true
            }
        }

    }


//    @SuppressLint("RestrictedApi")
//    private fun showPopup(view: View, person: Person) {
//        val context = view.context
//        val dao = AppDataBase.getInstance(context).dao()
//        val popupMenu = PopupMenu(context, view)
//        (context as Activity).menuInflater.inflate(R.menu.menu_only_delete, popupMenu.menu)
//        popupMenu.setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.action_delete -> {
//                    android.app.AlertDialog.Builder(context)
//                        .setTitle("Eliminar")
//                        .setMessage("¿Desea eliminar a " + person.info[KEY_NAME] + " de la lista?")
//                        .setNegativeButton("Cancelar", null)
//                        .setPositiveButton("Eliminar") { _, _ ->
//                            Completable.create {
//
//                                val headerMap = mutableMapOf<String, String>().apply {
//                                    this["Content-Type"] = "application/json"
//                                    this["operator"] = PreferencesManager(context).getId()
//                                    this["queue"] = queue.uuid!!
//                                    this["Authorization"] = Base64.encodeToString(
//                                        BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
//                                    ) ?: ""
//                                }
//
//                                val result = APIService.apiService.deleteCollaborator(
//                                    headers = headerMap,
//                                    data = Gson().toJson(person)
//                                ).execute()
//
//                                if (result.code() == 200) {
//                                    queue.collaborators.removeAll { ci -> ci == person.ci }
//                                    dao.insertQueue(queue)
//                                } else {
//                                    context.runOnUiThread {
//                                        Toast.makeText(context, result.message(), Toast.LENGTH_LONG)
//                                            .show()
//                                    }
//                                }
//
//                                it.onComplete()
//                            }
//                                .observeOn(Schedulers.io())
//                                .subscribeOn(Schedulers.io())
//                                .subscribe().addTo(CompositeDisposable())
//                        }
//                        .create()
//                        .show()
//                }
//            }
//            false
//        }
//        val wrapper = androidx.appcompat.view.ContextThemeWrapper(context, R.style.PopupWhite)
//        val menuPopupHelper =
//            MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, view)
//        menuPopupHelper.setForceShowIcon(true)
//        menuPopupHelper.show()
//    }
}