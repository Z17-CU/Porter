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
import cu.control.queue.adapters.viewHolders.ViewHolderClient
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

class AdapterPerson(private val queue: Queue) :
    RecyclerView.Adapter<ViewHolderClient>() {

    var contentList: List<Person> = ArrayList()
    private lateinit var context: Context
    private var myCi = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClient {
        context = parent.context
        myCi = PreferencesManager(context).getCi()
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

        holder.textViewName.text =
            person.info[KEY_NAME] as String? ?: "" + " " + person.info[KEY_LAST_NAME]
        holder.textViewID.text = person.ci + " - " + person.fv
        holder.textViewDate.text = ""
        holder.textViewReIntents.visibility = View.GONE

        holder.imageOwner.visibility =
            if (person.ci == queue.owner) View.VISIBLE else View.GONE

        holder.layoutBackground.setOnLongClickListener {
            if (person.ci != queue.owner && person.ci != myCi)
                showPopup(it, person)
            return@setOnLongClickListener true
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(view: View, person: Person) {
        val context = view.context
        val dao = AppDataBase.getInstance(context).dao()
        val popupMenu = PopupMenu(context, view)
        (context as Activity).menuInflater.inflate(R.menu.menu_only_delete, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Eliminar")
                        .setMessage("¿Desea eliminar a " + person.info[KEY_NAME] + " de la lista?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            Completable.create {

                                val headerMap = mutableMapOf<String, String>().apply {
                                    this["Content-Type"] = "application/json"
                                    this["operator"] = PreferencesManager(context).getId()
                                    this["queue"] = queue.uuid!!
                                    this["Authorization"] = Base64.encodeToString(
                                        BuildConfig.PORTER_SERIAL_KEY.toByteArray(), Base64.NO_WRAP
                                    ) ?: ""
                                }

                                val result = APIService.apiService.deleteCollaborator(
                                    headers = headerMap,
                                    data = Gson().toJson(person)
                                ).execute()

                                if (result.code() == 200) {
                                    queue.collaborators.removeAll { ci -> ci == person.ci }
                                    dao.insertQueue(queue)
                                } else {
                                    context.runOnUiThread {
                                        Toast.makeText(context, result.message(), Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }

                                it.onComplete()
                            }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe().addTo(CompositeDisposable())
                        }
                        .create()
                        .show()
                }
            }
            false
        }
        val wrapper = androidx.appcompat.view.ContextThemeWrapper(context, R.style.PopupWhite)
        val menuPopupHelper =
            MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, view)
        menuPopupHelper.setForceShowIcon(true)
        menuPopupHelper.show()
    }
}